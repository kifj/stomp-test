package x1.stomp.boundary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.logging.MDC;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.slf4j.Logger;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Action;
import x1.stomp.model.Share;
import x1.stomp.util.Logged;
import x1.stomp.util.StockMarket;
import x1.stomp.version.VersionData;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Session;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static x1.service.registry.Protocol.HTTP;
import static x1.service.registry.Protocol.HTTPS;
import static x1.service.registry.Technology.REST;

@Path(ShareResource.PATH)
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@RequestScoped
@Services(services = { @Service(technology = REST, value = RestApplication.ROOT + ShareResource.PATH,
    version = VersionData.APP_VERSION_MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
@Transactional(Transactional.TxType.REQUIRES_NEW)
@Logged
@Traced
@Tag(name = "Shares", description = "subscribe to shares on the stock market")
public class ShareResource {
  private static final String CORRELATION_ID = "correlationId";

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

  @Context
  private UriInfo uriInfo;

  @GET
  @Wrapped(element = "shares")
  @Formatted
  @Operation(description = "List all subscriptions")
  @ApiResponse(responseCode = "200", description = "All subscriptions",
      content = @Content(array = @ArraySchema(schema = @Schema(implementation = Share.class))))
  @Timed(name = "get-shares-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  public List<Share> listAllShares() {
    var shares = shareSubscription.list();
    shares.forEach(share -> addLinks(uriInfo.getBaseUriBuilder(), share));
    return shares;
  }

  @GET
  @Path("/{key}")
  @Formatted
  @Operation(description = "Find a share subscription")
  @ApiResponse(responseCode = "200", description = "Subscription found",
      content = @Content(schema = @Schema(implementation = Share.class)))
  @ApiResponse(responseCode = "404", description = "Subscription not found")
  @Timed(name = "get-share-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response findShare(@Parameter(description = "Stock symbol, see [quote.cnbc.com](https://quote.cnbc.com)",
      example = "BMW.DE") @PathParam("key") String key) {
    var share = shareSubscription.find(key);
    if (share.isPresent()) {
      log.info("findShare({}) returns {}", key, share.get());
      return Response.ok(addLinks(uriInfo.getBaseUriBuilder(), share.get())).build();
    } else {
      return Response.status(NOT_FOUND).build();
    }
  }

  @POST
  @Formatted
  @Operation(description = "Add a share to your list of subscriptions", operationId = "addShare")
  @ApiResponse(responseCode = "201", description = "Share queued for subscription",
          content = @Content(schema = @Schema(implementation = Share.class)))
  @ApiResponse(responseCode = "500", description = "Queuing failed")
  @ApiResponse(responseCode = "400", description = "Invalid data",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @Timed(name = "add-share-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response addShare(
      @Parameter(required = true,
          description = "The share which is will be added for subscription") @NotNull @Valid Share share,
      @Parameter(description = "provide a Correlation-Id header to receive a response for your operation when it finished.") 
      @HeaderParam(value = "Correlation-Id") String correlationId) {
    try (var session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
      try (var producer = session.createProducer(stockMarketQueue)) {
        var message = session.createObjectMessage(share);
        message.setJMSCorrelationID(correlationId != null ? correlationId : UUID.randomUUID().toString());        
        message.setStringProperty("type", "share");
        message.setStringProperty("action", Action.SUBSCRIBE.name());
        producer.send(message);
        MDC.put(CORRELATION_ID, message.getJMSCorrelationID());
        log.debug("message sent: {}", message);
      }
      var location = UriBuilder.fromPath("shares/{0}").build(share.getKey());
      return Response.created(location).build();
    } catch (JMSException e) {
      log.error(e.getErrorCode(), e);
      return Response.status(INTERNAL_SERVER_ERROR).build();
    } finally {
      MDC.remove(CORRELATION_ID);
    }
  }

  @DELETE
  @Path("/{key}")
  @Formatted
  @Operation(description = "Remove a subscription of a share")
  @ApiResponse(responseCode = "200", description = "Subscription removed",
      content = @Content(schema = @Schema(implementation = Share.class)))
  @ApiResponse(responseCode = "404", description = "Subscription was not found")
  @Timed(name = "remove-share-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  public Response removeShare(@Parameter(description = "Stock symbol", example = "GOOG") @PathParam("key") String key) {
    var share = shareSubscription.find(key);
    if (share.isPresent()) {
      shareSubscription.unsubscribe(share.get());
      return Response.ok(share.get()).build();
    } else {
      return Response.status(NOT_FOUND).build();
    }
  }

  private Share addLinks(UriBuilder baseUriBuilder, Share share) {
    var self = Link.fromUriBuilder(baseUriBuilder.clone().path(PATH).path(share.getKey())).rel("self").build();
    var delete = Link.fromUriBuilder(baseUriBuilder.clone().path(PATH).path(share.getKey())).rel("unsubscribe")
        .param("method", HttpMethod.DELETE).build();
    var quote = Link.fromUriBuilder(baseUriBuilder.clone().path(QuoteResource.PATH).path(share.getKey()))
        .rel("quote").build();
    share.setLinks(Arrays.asList(self, delete, quote));
    return share;
  }
}
