package x1.stomp.rest;

import java.util.List;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.slf4j.Logger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
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

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import x1.stomp.model.Share;
import x1.stomp.service.ShareSubscription;
import x1.stomp.util.StockMarket;

@Path("/shares")
@RequestScoped
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api(value = "/shares", description = "Manage your shares of at the stock market")
public class ShareResource {
	@Inject
	private Logger log;

	@Inject
	private ShareSubscription shareSubscription;

	@Inject
	@StockMarket
	private Connection connection;

	@Inject
	@StockMarket
	private Queue stockMarketQueue;

	@GET
	@Wrapped(element = "shares")
	@ApiOperation(value = "List all subscriptions")
	public List<Share> listAllShares() {
		return shareSubscription.list();
	}

	@GET
	@Path("/{key}")
	@ApiOperation(value = "Find a share subscription")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Subscription found"),
			@ApiResponse(code = 404, message = "Subscription not found") })
	public Response findShare(
			@ApiParam("Stock symbol (e.g. BMW.DE), see http://finance.yahoo.com/q") @PathParam("key") String key) {
		Share share = shareSubscription.find(key);
		if (share != null) {
			return Response.ok(share).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	@POST
	@ApiOperation(value = "Add share to your list of subscriptions")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Share queued for subscribing"),
			@ApiResponse(code = 500, message = "Queuing failed") })
	public Response addShare(
			@ApiParam(required = true, value = "The share which is will be added for supscription") @Valid Share share,
			@ApiParam(value = "provide a Correlation-Id header to receive a response for your operation when it finished.") @HeaderParam(value = "Correlation-Id") String correlationId) {

		Session session = null;
		try {
			log.info("Add share " + share);
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(stockMarketQueue);
			ObjectMessage message = session.createObjectMessage(share);
			message.setJMSCorrelationID(correlationId);
			producer.send(message);
			log.debug("message sent: " + message);
			return Response.ok(share).build(); // TODO created
		} catch (JMSException e) {
			log.error(null, e);
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		} finally {
			closeSession(session);
		}
	}

	@DELETE
	@Path("/{key}")
	@ApiOperation(value = "Remove a subscription to a share")
	@ApiResponses(value = { @ApiResponse(code = 200, message = "Subscription removed"),
			@ApiResponse(code = 404, message = "Subscription was not found") })
	public Response removeShare(@ApiParam("Stock symbol") @PathParam("key") String key) {
		Share share = shareSubscription.find(key);
		if (share != null) {
			shareSubscription.unsubscribe(share);
			return Response.ok(share).build();
		} else {
			return Response.status(Status.NOT_FOUND).build();
		}
	}

	private void closeSession(Session session) {
		try {
			if (session != null) {
				session.close();
			}
		} catch (JMSException e) {
			log.warn(null, e);
		}
	}
}
