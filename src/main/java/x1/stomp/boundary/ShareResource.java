package x1.stomp.boundary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.slf4j.Logger;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Action;
import x1.stomp.model.Share;
import x1.stomp.util.StockMarket;
import x1.stomp.util.VersionData;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static x1.service.registry.Protocol.HTTP;
import static x1.service.registry.Protocol.HTTPS;
import static x1.service.registry.Technology.REST;

@Path(ShareResource.PATH)
@RequestScoped
@Services(services = { @Service(technology = REST, value = RestApplication.ROOT
    + ShareResource.PATH, version = VersionData.MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
@Transactional(Transactional.TxType.REQUIRES_NEW)
public class ShareResource {
  protected static final String PATH = "/shares";

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
  @Operation(description = "List all subscriptions")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Subscription found", 
          content = @Content(schema = @Schema(implementation = Share[].class))) })
  @Timed(name = "get-shares-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  @Metered(name = "get-shares-meter", absolute = true)
  public List<Share> listAllShares() {
    return shareSubscription.list();
  }

  @GET
  @Path("/{key}")
  @Operation(description = "Find a share subscription")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Subscription found", 
          content = @Content(schema = @Schema(implementation = Share.class))),
      @ApiResponse(responseCode = "404", description = "Subscription not found") })
  @Timed(name = "get-share-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  @Metered(name = "get-share-meter", absolute = true)
  public Response findShare(
      @Parameter(description = "Stock symbol (e.g. BMW.DE), see https://quote.cnbc.com") @PathParam("key") String key) {
    Optional<Share> share = shareSubscription.find(key);
    if (share.isPresent()) {
      return Response.ok(share.get()).build();
    } else {
      return Response.status(NOT_FOUND).build();
    }
  }

  @POST
  @Operation(description = "Add a share to your list of subscriptions")
  @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Share queued for subscription"),
      @ApiResponse(responseCode = "500", description = "Queuing failed") })
  @Timed(name = "add-share-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  @Metered(name = "add-share-meter", absolute = true)
  public Response addShare(
      @Parameter(required = true, description = "The share which is will be added for subscription") @Valid Share share,
      @Parameter(description = "provide a Correlation-Id header to receive a response for your operation when it finished.")
      @HeaderParam(value = "Correlation-Id") String correlationId) {
    log.info("Add share {}", share);
    try (Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
      try (MessageProducer producer = session.createProducer(stockMarketQueue)) {
        ObjectMessage message = session.createObjectMessage(share);
        message.setJMSCorrelationID(correlationId);
        message.setStringProperty("type", "share");
        message.setStringProperty("action", Action.SUBSCRIBE.name());
        producer.send(message);
        log.debug("message sent: {}", message);
      }
      URI location = UriBuilder.fromPath("shares/{0}").build(share.getKey());
      return Response.created(location).build();
    } catch (JMSException e) {
      log.error(e.getErrorCode(), e);
      return Response.status(INTERNAL_SERVER_ERROR).build();
    }
  }

  @DELETE
  @Path("/{key}")
  @Operation(description = "Remove a subscription of a share")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Subscription removed",
          content = @Content(schema = @Schema(implementation = Share.class))),
      @ApiResponse(responseCode = "404", description = "Subscription was not found") })
  @Timed(name = "remove-share-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  @Metered(name = "remove-share-meter", absolute = true)
  public Response removeShare(@Parameter(description = "Stock symbol") @PathParam("key") String key) {
    Optional<Share> share = shareSubscription.find(key);
    if (share.isPresent()) {
      shareSubscription.unsubscribe(share.get());
      return Response.ok(share.get()).build();
    } else {
      return Response.status(NOT_FOUND).build();
    }
  }
}
