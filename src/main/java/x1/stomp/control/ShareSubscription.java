package x1.stomp.control;

import org.slf4j.Logger;
import x1.stomp.model.Share;
import x1.stomp.model.SubscriptionEvent;

import jakarta.ejb.Stateless;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.validation.constraints.NotNull;

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

  public Share subscribe(@NotNull Share share) {
    if (find(share.getKey()).isPresent()) {
      log.info("Subscription for {} already exists.", share);
      return share;
    }
    log.info("Subscribe to {}", share);
    em.persist(share);
    shareEvent.fire(new SubscriptionEvent(SUBSCRIBE, share.getKey()));
    return share;
  }

  public Share unsubscribe(@NotNull Share share) {
    log.info("Unsubscribe from {}", share);
    share = em.merge(share);
    em.remove(share);
    shareEvent.fire(new SubscriptionEvent(UNSUBSCRIBE, share.getKey()));
    return share;
  }

  public Optional<Share> find(@NotNull String key) {
    try {
      return Optional.of(em.createNamedQuery(Share.FIND_BY_KEY, Share.class).setParameter("key", key).getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  public List<Share> list() {
    return em.createNamedQuery(Share.LIST_ALL, Share.class).getResultList();
  }
}
