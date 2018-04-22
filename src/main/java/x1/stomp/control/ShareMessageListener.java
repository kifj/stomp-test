package x1.stomp.control;

import org.slf4j.Logger;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.model.Action;
import x1.stomp.model.Command;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;
import x1.stomp.util.VersionData;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.io.IOException;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static x1.service.registry.Protocol.*;
import static x1.service.registry.Technology.JMS;
import static x1.service.registry.Technology.STOMP;

@MessageDriven(name = "ShareMessageListener", activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/stocks"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
@Services(services = {
    @Service(technology = JMS, value = "java:/jms/queue/stocks", version = VersionData.MAJOR_MINOR, protocols = EJB),
    @Service(technology = STOMP, value = "jms.queue.stocksQueue", version = VersionData.MAJOR_MINOR, protocols = {
        STOMP_WS, STOMP_WSS }) })
public class ShareMessageListener implements MessageListener {

  @Inject
  private Logger log;

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteRetriever quoteRetriever;

  @Inject
  private JsonHelper jsonHelper;

  @Override
  public void onMessage(Message message) {
    try {
      if (message instanceof ObjectMessage) {
        log.info("Received ObjectMessage from queue: {}", message.getJMSDestination());
        onMessage((ObjectMessage) message);
      } else if (message instanceof BytesMessage) {
        log.info("Received BytesMessage from queue: {}", message.getJMSDestination());
        onMessage((BytesMessage) message);
      } else {
        log.warn("Message of wrong type: {}", message.getClass().getName());
      }
    } catch (Exception e) {
      throw new EJBException(e);
    }
  }

  private void onMessage(ObjectMessage message) throws JMSException {
    // TODO add more actions
    if (message.getStringProperty("type").equalsIgnoreCase("share") &&
            message.getStringProperty("action").equals(Action.SUBSCRIBE.name())) {
      subscribe((Share) message.getObject());
    } else {
      log.warn("Message of wrong type: {}", message);
    }
  }

  private void onMessage(BytesMessage message) throws JMSException, IOException {
    String body = message.readUTF();
    log.debug("Received message: {}", body);
    Command command = jsonHelper.fromJSON(body, Command.class);
    if (!isValid(command)) {
      log.warn("Incomplete command: {}", command);
      return;
    }
    switch (command.getAction()) {
      case SUBSCRIBE:
        subscribe(command.getKey());
        break;
      case UNSUBSCRIBE:
        unsubscribe(command.getKey());
        break;
      default:
        log.warn("Unknown command: {}", body);
        break;
    }
  }

  private boolean isValid(Command command) {
    return command != null && command.getAction() != null && isNotEmpty(command.getKey());
  }

  private void unsubscribe(String key) {
    log.info("Unsubscribe: {}", key);
    Optional<Share> share = shareSubscription.find(key);
    if (share.isPresent()) {
      shareSubscription.unsubscribe(share.get());
    } else {
      log.warn("Not found: {}", key);
    }
  }

  private void subscribe(String key) {
    Share share = new Share();
    share.setKey(key);
    subscribe(share);
  }

  private void subscribe(Share share) {
    log.info("Subscribe: {}", share.getKey());
    quoteRetriever.retrieveQuote(share).ifPresent(q -> shareSubscription.subscribe(q.getShare()));
  }

}
