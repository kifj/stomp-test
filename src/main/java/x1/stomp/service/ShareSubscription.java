package x1.stomp.service;

import java.util.List;

import x1.stomp.model.Share;

import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;

@Stateless
public class ShareSubscription {
	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	@Inject
	private Event<Share> shareEvent;

	public void subscribe(Share share) {
		if (find(share.getKey()) != null) {
			log.info("Subscription for " + share + " already exists.");
			return;
		}
		log.info("Subscribe " + share);
		em.persist(share);
		shareEvent.fire(share);
	}

	public void unsubscribe(Share share) {
		log.info("Unsubscribe " + share);
		share = em.merge(share);
		em.remove(share);
		shareEvent.fire(share);
	}

	public Share find(String key) {
		TypedQuery<Share> query = em.createQuery("from Share s where s.key = :key", Share.class);
		query.setParameter("key", key);
		return query.getSingleResult();
	}

	public List<Share> list() {
		return em.createQuery("from Share s order by s.name", Share.class).getResultList();
	}
}
