package x1.stomp.test;

public interface WebSocketTest {
  Integer getPortOffset();

  String getHost();

  WebSocketClient getWebSocketClient();

  String getPath();
}
