package x1.stomp.websockets;

import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;

@Singleton
@Startup
public class PingSender {
  private ByteBuffer ping;

  @PostConstruct
  public void setup() {
    ping = ByteBuffer.wrap("ping".getBytes(StandardCharsets.UTF_8));
  }

  @Inject
  private Logger log;

  @Inject
  private SessionHolder sessionHolder;

  @Schedule(hour = "*", minute = "*", second = "*/30", persistent = false)
  public void sendPing() {
    sessionHolder.values().forEach(session -> {
      try {
        session.getBasicRemote().sendPing(ping);
      } catch (ClosedChannelException e) {
        sessionHolder.remove(session.getId());
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    });
  }
}
