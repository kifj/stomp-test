package x1.stomp.test;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.*;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;

import static javax.websocket.CloseReason.CloseCodes.CLOSED_ABNORMALLY;
import static javax.websocket.CloseReason.CloseCodes.NORMAL_CLOSURE;

@ClientEndpoint
@ApplicationScoped
public class WebSocketClient {
  @Inject
  private Logger log;
  
  private Session session;
  private String lastMessage;

  public String getLastMessage() {
    return lastMessage;
  }

  @OnOpen
  public void onOpen(Session session) {
    this.session = session;
    log.debug("onOpen {}", session.getId());
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    log.info("onMessage {}: {}", session.getId(), message);
    lastMessage = message;
  }

  @OnClose
  public void onClose(Session session, CloseReason closeReason) {
    log.info("Session {} close because of {}", session.getId(), closeReason);
  }

  @SuppressWarnings("UnusedReturnValue")
  public WebSocketClient openConnection(String url) throws DeploymentException, IOException {
    var container = ContainerProvider.getWebSocketContainer();
    var uri = URI.create(url);
    container.connectToServer(this, uri);
    return this;
  }

  @SuppressWarnings("CatchMayIgnoreException")
  public void sendMessage(String message) throws IOException {
    try {
      session.getBasicRemote().sendText(message);
    } catch (IOException ex) {
      log.error(null, ex);
      try {
        session.close(new CloseReason(CLOSED_ABNORMALLY, ex.getMessage()));
      } catch (IOException ignored) {
        log.warn("Failed to close failed connection", ignored);
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
