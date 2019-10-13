package x1.stomp.websockets;

import javax.enterprise.context.ApplicationScoped;
import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class SessionHolder {
  private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

  public List<Session> values() {
    return new ArrayList<>(sessions.values());
  }

  @SuppressWarnings("UnusedReturnValue")
  public Session remove(String key) {
    return sessions.remove(key);
  }

  public void put(String key, Session value) {
    sessions.put(key, value);
  }
}
