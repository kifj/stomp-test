package x1.stomp.test;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebsocketExtension implements TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback {
  private static final Logger LOG = LoggerFactory.getLogger(WebsocketExtension.class);
  private String baseUrl;

  @Override
  public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
    if (testInstance instanceof WebSocketTest webSocketTest) {
      var host = webSocketTest.getHost();
      var port = 8080 + webSocketTest.getPortOffset();
      var path = webSocketTest.getPath();
      baseUrl = "ws://" + host + ":" + port + path;
    } else {
      LOG.warn("WebsocketExtension should be used with WebSocketTest: {}", testInstance);
    }
  }

  @Override
  public void beforeEach(ExtensionContext context) throws Exception {
    var testInstance = context.getRequiredTestInstance();
    if (testInstance instanceof WebSocketTest webSocketTest) {
      LOG.info("openConnection to baseUrl={}", baseUrl);
      if (webSocketTest.getWebSocketClient() != null) {
        webSocketTest.getWebSocketClient().openConnection(baseUrl);
        Thread.sleep(500);
      }
    }

  }

  @Override
  public void afterEach(ExtensionContext context) {
    var testInstance = context.getRequiredTestInstance();
    if (testInstance instanceof WebSocketTest webSocketTest && webSocketTest.getWebSocketClient() != null) {
      webSocketTest.getWebSocketClient().closeConnection();
    }
  }

}
