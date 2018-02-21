package x1.stomp.control;

import org.slf4j.Logger;
import x1.stomp.model.Share;
import x1.stomp.model.SubscriptionEvent;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

import static x1.stomp.model.Action.SUBSCRIBE;
import static x1.stomp.model.Action.UNSUBSCRIBE;

@Stateless
public class ShareSubscription {

  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @Inject
  private Event<SubscriptionEvent> shareEvent;

  public void subscribe(Share share) {
    if (find(share.getKey()).isPresent()) {
      log.info("Subscription for {} already exists.", share);
      return;
    }
    log.info("Subscribe to {}", share);
    em.persist(share);
    shareEvent.fire(new SubscriptionEvent(SUBSCRIBE, share.getKey()));
  }

  public void unsubscribe(Share share) {
    log.info("Unsubscribe from {}", share);
    share = em.merge(share);
    em.remove(share);
    shareEvent.fire(new SubscriptionEvent(UNSUBSCRIBE, share.getKey()));
  }

  public Optional<Share> find(String key) {
    try {
      TypedQuery<Share> query = em.createQuery(Share.FIND_BY_KEY, Share.class);
      query.setParameter("key", key);
      return Optional.of(query.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  public List<Share> list() {
    return em.createQuery(Share.LIST_ALL, Share.class).getResultList();
  }
}
