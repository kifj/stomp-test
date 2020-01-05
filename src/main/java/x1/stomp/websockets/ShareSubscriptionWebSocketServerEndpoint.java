package x1.stomp.websockets;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
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

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;

import static x1.service.registry.Protocol.*;
import static x1.service.registry.Technology.*;

@MessageDriven(name = "ShareSubscriptionWebSocketServerEndpoint", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
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

  @OnOpen
  public void onConnectionOpen(Session session) {
    log.info("Connection opened for session {}", session.getId());
    sessionHolder.put(session.getId(), session);
    quoteUpdater.updateQuotes();
  }

  @OnMessage
  public String onMessage(String message, Session session) throws IOException {
    log.debug("Received message: {}", message);
    String result = null;
    var command = jsonHelper.fromJSON(message, Command.class);
    if (command.getAction() == null || StringUtils.isEmpty(command.getKey())) {
      log.warn("Incomplete command: {}", command);
      return null;
    }
    switch (command.getAction()) {
      case SUBSCRIBE:
        Quote quote = subscribe(command.getKey());
        result = jsonHelper.toJSON(quote);
        break;
      case UNSUBSCRIBE:
        unsubscribe(command.getKey());
        break;
      default:
        log.warn("Unknown command: {}", message);
        break;
    }
    return result;
  }

  private void unsubscribe(String key) {
    log.info("Unsubscribe: {}", key);
    shareSubscription.find(key).ifPresent(shareSubscription::unsubscribe);
  }

  private Quote subscribe(String key) {
    log.info("Subscribe: {}", key);
    var share = new Share(key);
    var quote = quoteRetriever.retrieveQuote(share);
    quote.ifPresent(q -> shareSubscription.subscribe(q.getShare()));
    return quote.orElse(null);
  }

  @OnClose
  public void onConnectionClose(Session session) {
    log.info("Connection close for session {}", session.getId());
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
      TextMessage textMessage = (TextMessage) message;
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
