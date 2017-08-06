package x1.stomp.websockets;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.websocket.Session;

import org.slf4j.Logger;

@Singleton
@Startup
public class PingSender {
  private static final ByteBuffer PING = ByteBuffer.wrap("ping".getBytes());

  @Inject
  private Logger log;

  @Schedule(hour = "*", minute = "*", second = "*/30", persistent = false)
  public void sendPing() {
    for (Session session : new ArrayList<>(ShareSubscriptionWebSocketServerEndpoint.SESSIONS.values())) {
      try {
        session.getBasicRemote().sendPing(PING);
      } catch (ClosedChannelException e) {
        ShareSubscriptionWebSocketServerEndpoint.SESSIONS.remove(session.getId());
      } catch (Exception e) {
        log.error(null, e);
      }
    }
  }
}
