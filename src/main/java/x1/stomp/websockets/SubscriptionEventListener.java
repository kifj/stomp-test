package x1.stomp.websockets;

import org.slf4j.Logger;
import x1.stomp.model.SubscriptionEvent;
import x1.stomp.util.JsonHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import java.nio.channels.ClosedChannelException;

@ApplicationScoped
public class SubscriptionEventListener {

  @Inject
  private Logger log;

  @Inject
  private JsonHelper jsonHelper;

  @Inject
  private SessionHolder sessionHolder;

  public void onChangeSubscription(@Observes(during = TransactionPhase.AFTER_SUCCESS) SubscriptionEvent event) {
    log.info("Received subscription event {} for {}", event.getAction(), event.getKey());
    sessionHolder.values().forEach(session -> {
      try {
        session.getBasicRemote().sendText(jsonHelper.toJSON(event));
      } catch (ClosedChannelException e) {
        sessionHolder.remove(session.getId());
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    });
  }
}
