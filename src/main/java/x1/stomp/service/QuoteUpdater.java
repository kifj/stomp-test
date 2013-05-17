package x1.stomp.service;

import java.util.List;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;

import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;
import x1.stomp.util.StockMarket;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.xml.bind.JAXBException;

@Singleton
@Startup
public class QuoteUpdater {
	@Inject
	private Logger log;

	@Inject
	private QuoteRetriever quoteRetriever;

	@Inject
	private ShareSubscription shareSubscription;

	@Inject
	@StockMarket
	private Connection connection;

	@Inject
	@StockMarket
	private Topic quoteTopic;

	@Schedule(second = "*/30", minute = "*", hour = "*", persistent = false)
	public void updateQuotes() {
		List<Share> shares = shareSubscription.list();
		log.info("Update Quotes for " + shares.size() + " shares");
		Session session = null;
		try {
			List<Quote> quotes = quoteRetriever.retrieveQuotes(shares);

			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(quoteTopic);

			for (Quote quote : quotes) {
				log.debug("Sending message for " + quote);
				producer.send(createMessage(quote, session));
			}

		} catch (JMSException | JAXBException e) {
			log.error(null, e);
		} finally {
			closeSession(session);
		}
	}

	private Message createMessage(Quote quote, Session session) throws JMSException, JAXBException {
		TextMessage message = session.createTextMessage(JsonHelper.toJSON(quote));
		message.setStringProperty("type", "quote");
		return message;
	}

	private void closeSession(Session session) {
		try {
			if (null != session) {
				session.close();
			}
		} catch (JMSException e) {
			log.warn(null, e);
		}
	}
}
