package x1.stomp.boundary;

import java.net.URI;
import java.util.List;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.slf4j.Logger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiResponse;

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
import javax.ws.rs.core.UriBuilder;

import static x1.service.registry.Protocol.*;
import static x1.service.registry.Technology.*;

import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.model.Share;
import x1.stomp.control.ShareSubscription;
import x1.stomp.util.StockMarket;
import x1.stomp.util.VersionData;

@Path(ShareResource.PATH)
@RequestScoped
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api(value = ShareResource.PATH)
@Services(services = { @Service(technology = REST, value = RestApplication.ROOT
    + ShareResource.PATH, version = VersionData.MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
public class ShareResource {
  public static final String PATH = "/shares";
  
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
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Subscription found", response = Share[].class) })
  public List<Share> listAllShares() {
    return shareSubscription.list();
  }

  @GET
  @Path("/{key}")
  @ApiOperation(value = "Find a share subscription")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Subscription found", response = Share.class),
      @ApiResponse(code = 404, message = "Subscription not found") })
  public Response findShare(
      @ApiParam("Stock symbol (e.g. BMW.DE), see https://quote.cnbc.com") @PathParam("key") String key) {
    Share share = shareSubscription.find(key);
    if (share != null) {
      return Response.ok(share).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @POST
  @ApiOperation(value = "Add share to your list of subscriptions")
  @ApiResponses(value = { @ApiResponse(code = 201, message = "Share queued for subscription"),
      @ApiResponse(code = 500, message = "Queuing failed") })
  public Response addShare(
      @ApiParam(required = true, value = "The share which is will be added for subscription") @Valid Share share,
      @ApiParam(value = "provide a Correlation-Id header to receive a response for your operation when it finished.") 
      @HeaderParam(value = "Correlation-Id") String correlationId) {
    log.info("Add share " + share);
    try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
      try (MessageProducer producer = session.createProducer(stockMarketQueue)) {
        ObjectMessage message = session.createObjectMessage(share);
        message.setJMSCorrelationID(correlationId);
        producer.send(message);
        log.debug("message sent: " + message);
      }
      URI location = UriBuilder.fromPath("shares/{0}").build(share.getKey());
      return Response.created(location).build();
    } catch (JMSException e) {
      log.error(null, e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build();
    }
  }

  @DELETE
  @Path("/{key}")
  @ApiOperation(value = "Remove a subscription to a share")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Subscription removed", response = Share.class),
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

}
