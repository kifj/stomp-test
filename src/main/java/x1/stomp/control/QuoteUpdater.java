package x1.stomp.control;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timer;

import org.slf4j.Logger;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import x1.stomp.model.Quote;
import x1.stomp.util.JsonHelper;
import x1.stomp.util.StockMarket;

import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSProducer;
import jakarta.jms.Topic;
import jakarta.validation.constraints.NotNull;

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
  @WithSpan(kind = SpanKind.SERVER)
  public void onSchedule(Timer timer) {
    if (schedulerEnabled && timer.getNextTimeout().after(new Date())) {
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
