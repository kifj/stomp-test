package x1.stomp.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;

import static javax.websocket.CloseReason.CloseCodes.CLOSED_ABNORMALLY;
import static javax.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;

@ClientEndpoint
public class WebSocketClient {
  private static final Logger LOG = LoggerFactory.getLogger(WebSocketClient.class.getName());

  private Session session;
  private String lastMessage;

  private WebSocketClient() {
  }

  public String getLastMessage() {
    return lastMessage;
  }

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    LOG.debug("onOpen {}", session.getId());
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    LOG.info("onMessage {}: {}", session.getId(), message);
    lastMessage = message;
  }

  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    LOG.info("Session {} close because of {}", session.getId(), closeReason);
  }

  public static WebSocketClient openConnection(String url) throws DeploymentException, IOException {
    WebSocketContainer container = ContainerProvider.getWebSocketContainer();
    WebSocketClient client = new WebSocketClient();
    URI uri = URI.create(url);
    container.connectToServer(client, uri);
    return client;
  }

  public void sendMessage(String message) throws IOException {
    try {
      session.getBasicRemote().sendText(message);
    } catch (IOException ex) {
      LOG.error(null, ex);
      try {
        session.close(new CloseReason(CLOSED_ABNORMALLY, ex.getMessage()));
      } catch (IOException ignored) {
        LOG.warn("Failed to close failed connection", ignored);
      }
      throw ex;
    }
  }

  public void closeConnection() {
    try {
      session.close(new CloseReason(NORMAL_CLOSURE, "client requested"));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}