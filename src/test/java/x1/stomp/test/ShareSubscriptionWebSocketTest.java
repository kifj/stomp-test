package x1.stomp.test;

import static x1.stomp.model.Action.SUBSCRIBE;
import static x1.stomp.model.Action.UNSUBSCRIBE;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;

import x1.stomp.control.QuoteUpdater;
import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.model.SubscriptionEvent;
import x1.stomp.util.JsonHelper;
import x1.stomp.version.VersionData;

import static org.assertj.core.api.Assertions.assertThat;

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
    Thread.sleep(1500);
    var response = webSocketClient.getLastMessage();
    assertThat(response).isNotNull();
    log.debug("Received: {}", response);
    var received = jsonHelper.fromJSON(response, Quote.class);
    assertThat(received.getPrice()).isEqualTo(quote.getPrice());
    assertThat(received.getCurrency()).isEqualTo(quote.getCurrency());
    assertThat(received.getShare().getKey()).isEqualTo(TEST_SHARE);

    command.setAction(UNSUBSCRIBE);
    message = jsonHelper.toJSON(command);
    log.debug("Sending {}", command);
    webSocketClient.sendMessage(message);

    Thread.sleep(2500);
    response = webSocketClient.getLastMessage();
    assertThat(response).isNotNull();
    var event = jsonHelper.fromJSON(response, SubscriptionEvent.class);
    assertThat(event.getKey()).isEqualTo(TEST_SHARE);
    assertThat(event.getAction()).isEqualTo(UNSUBSCRIBE);
  }

}
