package x1.stomp.control;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timer;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.slf4j.Logger;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import x1.stomp.model.Quote;
import x1.stomp.util.JsonHelper;

import jakarta.inject.Inject;
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
  private JsonHelper jsonHelper;

  @Inject
  @ConfigProperty(name = "x1.stomp.control.QuoteUpdater/enable", defaultValue = "true")
  private boolean schedulerEnabled;

    @Inject
    @Channel("to-kafka")
    private Emitter<String> emitter;

  private int lastUpdatedCount;

  public int getLastUpdateCount() {
    return lastUpdatedCount;
  }

  @Schedule(second = "0", minute = "*/1", hour = "*", persistent = true, info = INFO_TEXT)
  @WithSpan(kind = SpanKind.CLIENT)
  public void onSchedule(Timer timer) {
    if (schedulerEnabled && timer.getNextTimeout().after(new Date())) {
      updateQuotes();
    }
  }

  public void updateQuotes() {
    lastUpdatedCount = 0;
    var shares = shareSubscription.list();
    log.info("Update quotes for {} shares", shares.size());
    quoteRetriever.retrieveQuotes(shares).forEach(this::send);
  }

  public void updateQuote(@NotNull Quote quote) {
    send(quote);
  }


  private void send(Quote quote) {
    try {
      log.debug("Sending message for {}", quote);

      var message = jsonHelper.toJSON(quote);
      emitter.send(message).toCompletableFuture().orTimeout(100, TimeUnit.MILLISECONDS).join();

      lastUpdatedCount++;
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }
}
