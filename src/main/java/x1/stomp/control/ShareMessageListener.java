package x1.stomp.control;

import org.slf4j.Logger;
import org.slf4j.MDC;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.EJBException;
import jakarta.inject.Inject;
import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.ObjectMessage;
import jakarta.ejb.MessageDriven;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.model.Action;
import x1.stomp.model.Command;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;
import x1.stomp.version.VersionData;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static x1.service.registry.Protocol.*;
import static x1.service.registry.Technology.JMS;
import static x1.service.registry.Technology.STOMP;

@MessageDriven(name = "ShareMessageListener",
    activationConfig = { @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/stocks"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Services(services = {
    @Service(technology = JMS, value = "java:/jms/queue/stocks", version = VersionData.APP_VERSION_MAJOR_MINOR,
        protocols = EJB),
    @Service(technology = STOMP, value = "jms.queue.stocksQueue", version = VersionData.APP_VERSION_MAJOR_MINOR,
        protocols = { STOMP_WS, STOMP_WSS }) })
public class ShareMessageListener implements MessageListener {
  private static final String CORRELATION_ID = "correlationId";

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
      var correlationId = message.getJMSCorrelationID();
      MDC.put(CORRELATION_ID, correlationId);
      if (message instanceof ObjectMessage objectMessage) {
        log.info("Received ObjectMessage {} from queue: {}", correlationId, message.getJMSDestination());
        handleMessage(objectMessage);
      } else if (message instanceof BytesMessage byteMessage) {
        log.info("Received BytesMessage {} from queue: {}", correlationId, message.getJMSDestination());
        handleMessage(byteMessage);
      } else {
        log.warn("Message {} of wrong type: {}", correlationId, message.getClass().getName());
      }
    } catch (JMSException | IOException e) {
      log.error(e.getMessage(), e);
      throw new EJBException(e);
    } finally {
      MDC.remove(CORRELATION_ID);
    }
  }

  private void handleMessage(ObjectMessage message) throws JMSException {
    if (message.getStringProperty("type").equalsIgnoreCase("share")) {
      var action = Action.valueOf(message.getStringProperty("action"));
      switch (action) {
        case SUBSCRIBE -> subscribe((Share) message.getObject());
        case UNSUBSCRIBE -> shareSubscription.unsubscribe((Share) message.getObject());
        default -> log.warn("Unsupported action: {}", action);
      }
    } else {
      log.warn("Message of wrong type: {}", message);
    }
  }

  private void handleMessage(BytesMessage message) throws JMSException, IOException {
    var body = message.readUTF();
    log.debug("Received message: {}", body);
    var command = jsonHelper.fromJSON(body, Command.class);
    if (!isValid(command)) {
      log.warn("Incomplete command: {}", command);
      return;
    }
    switch (command.getAction()) {
      case SUBSCRIBE -> subscribe(command.getKey());
      case UNSUBSCRIBE -> unsubscribe(command.getKey());
      default -> log.warn("Unknown command: {}", body);
    }
  }

  private boolean isValid(Command command) {
    return command != null && command.getAction() != null && isNotEmpty(command.getKey());
  }

  private void unsubscribe(String key) {
    log.info("Unsubscribe: {}", key);
    shareSubscription.find(key).ifPresentOrElse(shareSubscription::unsubscribe, () -> log.warn("Not found: {}", key));
  }

  private void subscribe(String key) {
    subscribe(new Share(key));
  }

  private void subscribe(Share share) {
    log.info("Subscribe: {}", share.getKey());
    quoteRetriever.retrieveQuote(share).ifPresent(q -> shareSubscription.subscribe(q.getShare()));
  }

}
