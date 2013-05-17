package x1.stomp.rest;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.slf4j.Logger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.persistence.NoResultException;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import x1.stomp.model.Share;
import x1.stomp.service.ShareSubscription;
import x1.stomp.util.StockMarket;

@Path("/shares")
@RequestScoped
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class ShareResource {
	@Inject
	private Logger log;

	@Inject
	private ShareSubscription shareSubscription;

	@Inject
	private Validator validator;

	@Inject
	@StockMarket
	private Connection connection;

	@Inject
	@StockMarket
	private Queue stockMarketQueue;

	@GET
	@Wrapped(element = "orders")
	public List<Share> listAllShares() {
		return shareSubscription.list();
	}

	@GET
	@Path("/{key}")
	public Share findShare(@PathParam("key") String key) {
		return shareSubscription.find(key);
	}

	@POST
	public Response addShare(Share share, @HeaderParam(value = "Correlation-Id") String correlationId) {
		validate(share);
		Session session = null;
		try {
			log.info("Add share " + share);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(stockMarketQueue);
			ObjectMessage message = session.createObjectMessage(share);
			message.setJMSCorrelationID(correlationId);
			producer.send(message);
			log.debug("message sent: " + message);
			return Response.ok(share).build();
		} catch (JMSException e) {
			log.error(null, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			closeSession(session);
		}
	}

	@DELETE
	@Path("/{key}")
	public Response removeShare(@PathParam("key") String key) {
		try {
			Share share = shareSubscription.find(key);
			shareSubscription.unsubscribe(share);
			return Response.ok(share).build();
		} catch (NoResultException e) {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	private void validate(Share share) {
		Set<ConstraintViolation<Share>> violations = validator.validate(share);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(new HashSet<ConstraintViolation<?>>(violations));
		}
	}

	private void closeSession(Session session) {
		try {
			if (null != session) {
				session.close();
			}
		} catch (JMSException e) {
			log.warn(null, e);
		}
	}
}
