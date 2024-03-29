package x1.stomp.websockets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import io.opentelemetry.api.trace.Tracer;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.QuoteRetriever;
import x1.stomp.control.QuoteUpdater;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;
import x1.stomp.version.VersionData;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.inject.Inject;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static x1.service.registry.Protocol.*;
import static x1.service.registry.Technology.*;

@MessageDriven(name = "ShareSubscriptionWebSocketServerEndpoint", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Topic"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/topic/quotes"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@ServerEndpoint("/ws/stocks")
@Services(services = {
        @Service(technology = JMS, value = "java:/jms/topic/quotes",
                version = VersionData.APP_VERSION_MAJOR_MINOR, protocols = EJB),
        @Service(technology = WEBSOCKETS, value = "/ws/stocks",
                version = VersionData.APP_VERSION_MAJOR_MINOR, protocols = {WS, WSS}),
        @Service(technology = STOMP, value = "jms.topic.quotesTopic",
                version = VersionData.APP_VERSION_MAJOR_MINOR, protocols = {STOMP_WS, STOMP_WSS})
})
public class ShareSubscriptionWebSocketServerEndpoint implements MessageListener {

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteRetriever quoteRetriever;
  
  @Inject
  private QuoteUpdater quoteUpdater;

  @Inject
  private Logger log;

  @Inject
  private JsonHelper jsonHelper;

  @Inject
  private SessionHolder sessionHolder;

  @Inject
  private Tracer tracer;
  
  @OnOpen
  public void onConnectionOpen(Session session) {
    log.debug("Connection opened for session {}", session.getId());
    sessionHolder.put(session.getId(), session);
    quoteUpdater.updateQuotes();
  }

  @OnMessage
  public String onMessage(String message, Session session) throws IOException {
    log.debug("Received message: {}", message);
    var command = jsonHelper.fromJSON(message, Command.class);
    if (command.getAction() == null || StringUtils.isEmpty(command.getKey())) {
      log.warn("Incomplete command: {}", command);
      return null;
    }
    switch (command.getAction()) {
      case SUBSCRIBE ->  jsonHelper.toJSON(subscribe(command.getKey()).orElse(null));
      case UNSUBSCRIBE -> unsubscribe(command.getKey());
      default -> log.warn("Unknown command: {}", message);
    }
    return null;
  }

  private void unsubscribe(String key) {
    log.info("Unsubscribe: {}", key);
    var span = tracer.spanBuilder("/ws/stocks").setAttribute("command", "unsubscribe").setAttribute("key", key).startSpan();
    try {
      shareSubscription.find(key).ifPresent(shareSubscription::unsubscribe);
    } finally {
      span.end();
    }
  }

  private Optional<Quote> subscribe(String key) {    
    log.info("Subscribe: {}", key);
    var span = tracer.spanBuilder("/ws/stocks").setAttribute("command", "subscribe").setAttribute("key", key).startSpan();
    try {
      var share = new Share(key);
      var quote = quoteRetriever.retrieveQuote(share);
      quote.ifPresent(q -> shareSubscription.subscribe(q.getShare()));
      return quote;
    } finally {
      span.end();
    }
  }

  @OnClose
  public void onConnectionClose(Session session) {
    log.debug("Connection close for session {}", session.getId());
    sessionHolder.remove(session.getId());
  }

  @OnError
  public void error(Session session, Throwable t) {
    log.warn("Connection error for session {} with error {}", session.getId(), t.getMessage());
    sessionHolder.remove(session.getId());
  }

  @Override
  public void onMessage(Message message) {
    try {
      log.debug("Received quote for {}", message.getStringProperty("key"));
      var textMessage = (TextMessage) message;
      sessionHolder.values().forEach(session -> sendMessage(textMessage, session));
    } catch (JMSException e) {
      log.error(e.getErrorCode(), e);
    }
  }

  @OnMessage
  public void onMessage(PongMessage message, Session session) {
    String answer = null;
    if (message.getApplicationData().hasArray()) {
      answer = new String(message.getApplicationData().array(), StandardCharsets.UTF_8);
    }
    log.debug("Received pong [{}]", answer);
  }

  private void sendMessage(TextMessage textMessage, Session session) {
    try {
      session.getBasicRemote().sendText(textMessage.getText());
    } catch (ClosedChannelException e) {
      sessionHolder.remove(session.getId());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
