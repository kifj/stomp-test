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
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
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
  @JMSConnectionFactory("java:/JmsXA")
  private JMSContext context;

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
    var quotes = quoteRetriever.retrieveQuotes(shares);
    quotes.forEach(quote -> {
      try {
        log.debug("Sending message for {}", quote);
        send(quote, context.createProducer(), quoteTopic);
        lastUpdatedCount++;
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    });
  }

  public void updateQuote(@NotNull Quote quote) {
    try {
      log.debug("Sending message for {}", quote);
      send(quote, context.createProducer(), quoteTopic);
      lastUpdatedCount++;
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }
  }

  private void send(Quote quote, JMSProducer producer, Topic topic) throws IOException {
    producer.setJMSCorrelationID(UUID.randomUUID().toString()).setProperty("type", "quote")
        .setProperty("key", quote.getShare().getKey()).send(topic, jsonHelper.toJSON(quote));
  }
}
