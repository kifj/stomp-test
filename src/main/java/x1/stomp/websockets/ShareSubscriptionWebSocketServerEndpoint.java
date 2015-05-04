package x1.stomp.websockets;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.service.QuoteRetriever;
import x1.stomp.service.ShareSubscription;
import x1.stomp.util.JsonHelper;

@MessageDriven(name = "ShareSubscriptionWebSocketServerEndpoint", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/quotes"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@ServerEndpoint("/ws/stocks")
public class ShareSubscriptionWebSocketServerEndpoint implements MessageListener {
  static final Map<String, Session> SESSIONS = new HashMap<>();

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteRetriever quoteRetriever;

  @Inject
  private Logger log;

  @OnOpen
  public void onConnectionOpen(Session session) {
    log.info("Connection opened for session " + session.getId());
    SESSIONS.put(session.getId(), session);
  }

  @OnMessage
  public String onMessage(String message, Session session) throws IOException, JAXBException {
    log.debug("Received message: {}", message);
    String result = null;
    Command command = JsonHelper.fromJSON(message, Command.class);
    if (StringUtils.isEmpty(command.getAction()) || StringUtils.isEmpty(command.getKey())) {
      log.warn("Incomplete command: {}", command);
      return result;
    }
    switch (command.getAction().toUpperCase()) {
    case "SUBSCRIBE":
      Quote quote = subscribe(command.getKey());
      result = JsonHelper.toJSON(quote);
      break;
    case "UNSUBSCRIBE":
      unsubscribe(command.getKey());
      break;
    default:
      log.warn("Unknown command: {}", message);
      break;
    }
    return result;
  }

  private void unsubscribe(String key) {
    try {
      log.info("Unsubscribe: {}", key);
      Share share = shareSubscription.find(key);
      if (share != null) {
        shareSubscription.unsubscribe(share);
      }
    } catch (Exception e) {
      log.warn("Unsubscribe {} failed: {}", key, e.getMessage());
    }
  }

  private Quote subscribe(String key) {
    Quote quote = null;
    try {
      log.info("Subscribe: {}", key);
      Share share = new Share();
      share.setKey(key);
      quote = quoteRetriever.retrieveQuote(share);
      if (quote != null) {
        shareSubscription.subscribe(quote.getShare());
      }
    } catch (Exception e) {
      log.warn("Subscribe {} failed: {}", key, e.getMessage());
    }
    return quote;
  }

  @OnClose
  public void onConnectionClose(Session session) {
    log.info("Connection close for session {}", session.getId());
    SESSIONS.remove(session.getId());
  }

  @OnError
  public void error(Session session, Throwable t) {
    SESSIONS.remove(session);
    log.info("Connection error for session {}", session.getId());
  }

  @Override
  public void onMessage(Message message) {
    try {
      log.debug("Received quote for {}", message.getStringProperty("key"));
      TextMessage textMessage = (TextMessage) message;
      for (Session session : new ArrayList<>(SESSIONS.values())) {
        try {
          session.getBasicRemote().sendText(textMessage.getText());
        } catch (ClosedChannelException e) {
          SESSIONS.remove(session.getId());
        } catch (Exception e) {
          log.error(null, e);
        }
      }
    } catch (JMSException e) {
      log.error(null, e);
    }
  }
}