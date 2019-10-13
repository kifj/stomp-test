package x1.stomp.control;

import java.io.IOException;
import java.util.UUID;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;

import x1.stomp.model.Quote;
import x1.stomp.util.JsonHelper;
import x1.stomp.util.StockMarket;

import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.Topic;
import javax.validation.constraints.NotNull;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@Singleton
@Startup
public class QuoteUpdater {
  private static final String INFO_TEXT = "updateQuotes";

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

  @Inject
  private JsonHelper jsonHelper;

  @Inject
  @ConfigProperty(name = "x1.stomp.control.QuoteUpdater/enable", defaultValue = "true")
  private boolean schedulerEnabled;

  private int lastUpdatedCount;

  public int getLastUpdateCount() {
    return lastUpdatedCount;
  }

  @Schedule(second = "0", minute = "*/1", hour = "*", persistent = true, info = INFO_TEXT)
  public void onSchedule() {
    if (schedulerEnabled) {
      updateQuotes();
    }
  }

  public void updateQuotes() {
    lastUpdatedCount = 0;
    var shares = shareSubscription.list();
    log.info("Update quotes for {} shares", shares.size());
    try (var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
      try (var producer = session.createProducer(quoteTopic)) {
        var quotes = quoteRetriever.retrieveQuotes(shares);
        quotes.forEach(quote -> {
          try {
            log.debug("Sending message for {}", quote);
            producer.send(createMessage(quote, session));
            lastUpdatedCount++;
          } catch (JMSException | IOException e) {
            log.error(null, e);
          }
        });
      }
    } catch (JMSException e) {
      log.error(e.getErrorCode(), e);
    }
  }

  public void updateQuote(@NotNull Quote quote) {
    try (var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
      try (var producer = session.createProducer(quoteTopic)) {
        log.debug("Sending message for {}", quote);
        producer.send(createMessage(quote, session));
      }
      lastUpdatedCount++;
    } catch (JMSException | IOException e) {
      log.error(null, e);
    }
  }

  private Message createMessage(Quote quote, Session session) throws JMSException, IOException {
    var message = session.createTextMessage(jsonHelper.toJSON(quote));
    message.setJMSCorrelationID(UUID.randomUUID().toString());
    message.setStringProperty("type", "quote");
    message.setStringProperty("key", quote.getShare().getKey());
    return message;
  }
}
