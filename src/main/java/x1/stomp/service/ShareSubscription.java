package x1.stomp.service;

import java.util.List;

import x1.stomp.model.Share;
import x1.stomp.model.SubscriptionEvent;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;

@Stateless
public class ShareSubscription {
  @Inject
  private Logger log;

  @Inject
  private EntityManager em;

  @Inject
  private Event<SubscriptionEvent> shareEvent;

  public void subscribe(Share share) {
    if (find(share.getKey()) != null) {
      log.info("Subscription for {} already exists.", share);
      return;
    }
    log.info("Subscribe {}", share);
    em.persist(share);
    shareEvent.fire(new SubscriptionEvent(share.getKey(), "subscribe"));
  }

  public void unsubscribe(Share share) {
    log.info("Unsubscribe {}", share);
    share = em.merge(share);
    em.remove(share);
    shareEvent.fire(new SubscriptionEvent(share.getKey(), "unsubscribe"));
  }

  public Share find(String key) {
    try {
      TypedQuery<Share> query = em.createQuery("from Share s where s.key = :key", Share.class);
      query.setParameter("key", key);
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    }
  }

  public List<Share> list() {
    return em.createQuery("from Share s order by s.name", Share.class).getResultList();
  }
}
