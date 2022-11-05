package x1.stomp.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import x1.stomp.control.QuoteUpdater;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Share;

import jakarta.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ShareSubscription Test")
public class ShareSubscriptionTest extends AbstractIT {

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteUpdater quoteUpdater;

  @Inject
  private Logger log;

  @Test
  public void testSubscribe() {
    var share = new Share();
    share.setKey("MSFT");
    share.setName("Microsoft Corporation");

    while (true) {
      var existing = shareSubscription.find(share.getKey());
      if (existing.isPresent()) {
        shareSubscription.unsubscribe(existing.get());
      } else {
        break;
      }
    }

    shareSubscription.subscribe(share);
    assertThat(share.getId()).isNotNull();
    assertThat(share.getVersion()).isNotNull();
    log.info("{} was persisted with id {}", share.getName(), share.getId());
    // nothing happens
    shareSubscription.subscribe(share);

    var s = shareSubscription.find(share.getKey());
    assertThat(s).isPresent();
    assertThat(shareSubscription.list()).size().isEqualTo(1);
    share = s.get();
    shareSubscription.unsubscribe(share);
    assertThat(shareSubscription.find(share.getKey())).isNotPresent();
  }

  @Test
  public void testQuoteUpdater() throws Exception {
    var share = new Share();
    share.setKey("GOOG");
    share.setName("Google");
    shareSubscription.subscribe(share);
    quoteUpdater.updateQuotes();
    assertThat(quoteUpdater.getLastUpdateCount()).isEqualTo(1);
    Thread.sleep(3000);
    shareSubscription.find(share.getKey()).ifPresent(shareSubscription::unsubscribe);
  }
}
