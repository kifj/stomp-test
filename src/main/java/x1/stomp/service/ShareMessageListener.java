package x1.stomp.service;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.ejb.ActivationConfigProperty;

import x1.service.registry.Protocol;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.service.registry.Technology;
import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;
import x1.stomp.util.VersionData;

@MessageDriven(name = "ShareMessageListener", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
    @ActivationConfigProperty(propertyName = "destination", propertyValue = "java:/jms/queue/stocks"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
@Services(services = {
    @Service(technology = Technology.JMS, 
        value = "java:/jms/queue/stocks", 
        version = VersionData.MAJOR_MINOR, 
        protocols = Protocol.EJB),
    @Service(technology = Technology.STOMP, 
        value = "jms.queue.stocksQueue", 
        version = VersionData.MAJOR_MINOR, 
        protocols = { Protocol.STOMP_WS, Protocol.STOMP_WSS })
})
public class ShareMessageListener implements MessageListener {
  @Inject
  private Logger log;

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteRetriever quoteRetriever;

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
    Share share = (Share) message.getObject();
    Quote quote = quoteRetriever.retrieveQuote(share);
    if (quote != null) {
      shareSubscription.subscribe(quote.getShare());
    }
  }

  private void onMessage(BytesMessage message) throws JMSException, IOException {
    byte[] bytes = new byte[(int) message.getBodyLength()];
    message.readBytes(bytes);
    String body = new String(bytes, "UTF-8");
    log.debug("Received message: {}", body);
    Command command = JsonHelper.fromJSON(body, Command.class);
    if (StringUtils.isEmpty(command.getAction()) || StringUtils.isEmpty(command.getKey())) {
      log.warn("Incomplete command: {}", command);
      return;
    }
    switch (command.getAction().toUpperCase()) {
    case "SUBSCRIBE":
      subscribe(command.getKey());
      break;
    case "UNSUBSCRIBE":
      unsubscribe(command.getKey());
      break;
    default:
      log.warn("Unknown command: {}", body);
      break;
    }
  }

  private void unsubscribe(String key) {
    log.info("Unsubscribe: {}", key);
    Share share = shareSubscription.find(key);
    if (share != null) {
      shareSubscription.unsubscribe(share);
    } else {
      log.warn("Not found: {}", key);
    }
  }

  private void subscribe(String key) {
    log.info("Subscribe: {}", key);
    Share share = new Share();
    share.setKey(key);
    Quote quote = quoteRetriever.retrieveQuote(share);
    if (quote != null) {
      shareSubscription.subscribe(quote.getShare());
    }
  }

}
