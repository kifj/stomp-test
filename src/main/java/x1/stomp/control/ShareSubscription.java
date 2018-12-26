package x1.stomp.control;

import org.slf4j.Logger;
import x1.stomp.model.Share;
import x1.stomp.model.SubscriptionEvent;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.validation.constraints.NotNull;

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
      var query = em.createNamedQuery(Share.FIND_BY_KEY, Share.class);
      query.setParameter("key", key);
      return Optional.of(query.getSingleResult());
    } catch (NoResultException e) {
      return Optional.empty();
    }
  }

  public List<Share> list() {
    return em.createNamedQuery(Share.LIST_ALL, Share.class).getResultList();
  }
}
