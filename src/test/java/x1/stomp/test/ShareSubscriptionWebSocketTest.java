package x1.stomp.test;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.opentest4j.AssertionFailedError;
import org.slf4j.Logger;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import x1.stomp.control.QuoteUpdater;
import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.model.SubscriptionEvent;
import x1.stomp.util.JsonHelper;
import x1.stomp.version.VersionData;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static x1.stomp.model.Action.SUBSCRIBE;
import static x1.stomp.model.Action.UNSUBSCRIBE;

@DisplayName("ShareSubscription WebSocket Test")
@ExtendWith(WebsocketExtension.class)
public class ShareSubscriptionWebSocketTest extends AbstractIT implements WebSocketTest {
    private static final String TEST_SHARE = "MSFT";

    @Inject
    private Logger log;

    @Inject
    private JsonHelper jsonHelper;

    @EJB
    private QuoteUpdater quoteUpdater;

    @Inject
    private WebSocketClient webSocketClient;

    @Override
    public WebSocketClient getWebSocketClient() {
        return webSocketClient;
    }

    @Override
    public String getPath() {
        return "/" + VersionData.APP_NAME_MAJOR_MINOR + "/ws/stocks";
    }

    @Test
    void testWebSocket() throws Exception {
        var command = new Command(SUBSCRIBE, TEST_SHARE);
        var message = jsonHelper.toJSON(command);
        log.debug("Sending {}", command);
        webSocketClient.sendMessage(message);
        Thread.sleep(2500);

        var quote = new Quote();
        quote.setCurrency("USD");
        quote.setPrice(10.0f);
        var share = new Share(TEST_SHARE);
        quote.setShare(share);
        quoteUpdater.updateQuote(quote);
        getLastResponse(Duration.ofMillis(1000), (r) -> {
            assertThat(r).isNotNull();
            log.debug("Received: {}", r);
            var received = fromJSON(r, Quote.class);
            assertThat(received).isNotNull();
            assertThat(received.getPrice()).isEqualTo(quote.getPrice());
            assertThat(received.getCurrency()).isEqualTo(quote.getCurrency());
            assertThat(received.getShare().getKey()).isEqualTo(TEST_SHARE);
        });

        command.setAction(UNSUBSCRIBE);
        message = jsonHelper.toJSON(command);
        log.debug("Sending {}", command);
        webSocketClient.sendMessage(message);

        getLastResponse(Duration.ofMillis(1000), (r) -> {
            assertThat(r).isNotNull();
            var event = fromJSON(r, SubscriptionEvent.class);
            assertThat(event).isNotNull();
            assertThat(event.getKey()).isEqualTo(TEST_SHARE);
            assertThat(event.getAction()).isEqualTo(UNSUBSCRIBE);
        });
    }

    private <T> T fromJSON(String r, Class<T> type) {
        try {
            return jsonHelper.fromJSON(r, type);
        } catch (IOException e) {
            log.warn(e.getMessage());
            return null;
        }
    }

    private void getLastResponse(Duration timeout, Consumer<String> asserter) {
        Awaitility.waitAtMost(5, TimeUnit.SECONDS).with()
                .atLeast(timeout).pollInterval(Duration.of(1, ChronoUnit.SECONDS)).until(() -> {
            try {
                var response = webSocketClient.getLastMessage();
                asserter.accept(response);
                return true;
            } catch (AssertionFailedError e) {
                log.info("{}: {}", e.getClass().getName(), e.getMessage());
                return false;
            } catch (Exception e) {
                log.warn("{}: {}", e.getClass().getName(), e.getMessage());
                return false;
            }
        });
    }

}
