package x1.stomp.websockets;

import java.nio.channels.ClosedChannelException;
import java.util.ArrayList;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.websocket.Session;

import org.slf4j.Logger;

import x1.stomp.model.SubscriptionEvent;
import x1.stomp.util.JsonHelper;

public class SubscriptionEventListener {

	@Inject
	private Logger log;

	public void onChangeSubscription(
			@Observes(during = TransactionPhase.AFTER_SUCCESS) SubscriptionEvent event) {
		log.info("Received subscription event {} for {}", event.getAction(), event.getKey());
		for (Session session : new ArrayList<>(ShareSubscriptionWebSocketServerEndpoint.SESSIONS.values())) {
			try {
				session.getBasicRemote().sendText(JsonHelper.toJSON(event));
			} catch (ClosedChannelException e) {
				ShareSubscriptionWebSocketServerEndpoint.SESSIONS.remove(session.getId());
			} catch (Exception e) {
				log.error(null, e);
			}
		}
	}
}
