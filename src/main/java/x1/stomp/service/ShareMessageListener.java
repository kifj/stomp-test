package x1.stomp.service;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

import javax.ejb.EJBException;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.NoResultException;
import javax.ejb.ActivationConfigProperty;

import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;

@MessageDriven(name = "ShareMessageListener", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/stocks"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
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
				log.info("Received ObjectMessage from queue: " + message.getJMSDestination());
				onMessage((ObjectMessage) message);
			} else if (message instanceof BytesMessage) {
				log.info("Received BytesMessage from queue: " + message.getJMSDestination());
				onMessage((BytesMessage) message);
			} else {
				log.warn("Message of wrong type: " + message.getClass().getName());
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

	private void onMessage(BytesMessage message) throws Exception {
		byte[] bytes = new byte[(int) message.getBodyLength()];
		message.readBytes(bytes);
		String body = new String(bytes);
		log.debug("Received message: " + body);
		Command command = JsonHelper.fromJSON(body, Command.class);
		if (StringUtils.isEmpty(command.getAction()) || StringUtils.isEmpty(command.getKey())) {
			log.warn("Incomplete command: " + command);
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
			log.warn("Unknown command: " + body);
			break;
		}
	}

	private void unsubscribe(String key) {
		try {
			log.info("Unsubscribe: " + key);			
			Share share = shareSubscription.find(key);
			shareSubscription.unsubscribe(share);
		} catch (NoResultException e) {
			log.warn("Not found: " + key);
		}
	}
	
	private void subscribe(String key) {
		log.info("Subscribe: " + key);
		Share share = new Share();
		share.setKey(key);
		Quote quote = quoteRetriever.retrieveQuote(share);
		if (quote != null) {
			shareSubscription.subscribe(quote.getShare());
		}
	}

}
