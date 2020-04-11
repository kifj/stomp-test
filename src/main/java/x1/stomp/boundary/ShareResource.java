package x1.stomp.boundary;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.slf4j.Logger;
import org.slf4j.MDC;

import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Action;
import x1.stomp.model.Share;
import x1.stomp.model.ShareWrapper;
import x1.stomp.util.Logged;
import x1.stomp.util.MDCKey;
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
import static javax.ws.rs.core.MediaType.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static javax.ws.rs.core.Response.Status.*;
import static x1.service.registry.Protocol.HTTP;
import static x1.service.registry.Protocol.HTTPS;
import static x1.service.registry.Technology.REST;

@Path(ShareResource.PATH)
@Produces({ APPLICATION_JSON, APPLICATION_XML })
@Consumes({ APPLICATION_JSON, APPLICATION_XML })
@RequestScoped
@Services(services = { @Service(technology = REST, value = RestApplication.ROOT + ShareResource.PATH,
    version = VersionData.APP_VERSION_MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
@Transactional(Transactional.TxType.REQUIRES_NEW)
@Logged
@Traced
@Tag(name = "Shares", description = "subscribe to shares on the stock market")
public class ShareResource {
  private static final String MDC_KEY = "share";
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
  @Parameters({ @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_CALLER_ID),
          @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_REQUEST_ID) })
  @APIResponse(responseCode = "200", description = "All subscriptions", content = {
          @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = Share.class), mediaType = APPLICATION_JSON),
          @Content(schema = @Schema(implementation = ShareWrapper.class), mediaType = APPLICATION_XML)})
  @SimplyTimed(name = "get-shares", absolute = true, unit = MetricUnits.SECONDS, tags = {"interface=ShareResource"})
  @Bulkhead(value = 5)
  public List<Share> listAllShares() {
    var shares = shareSubscription.list();
    shares.forEach(share -> addLinks(uriInfo.getBaseUriBuilder(), share));
    return shares;
  }

  @GET
  @Path("/{key}")
  @Formatted
  @Operation(description = "Find a share subscription")
  @Parameters({ @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_CALLER_ID),
          @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_REQUEST_ID) })
  @APIResponse(responseCode = "200", description = "Subscription found",
      content = @Content(schema = @Schema(implementation = Share.class)))
  @APIResponse(responseCode = "404", description = "Subscription not found")
  @SimplyTimed(name = "get-share", absolute = true, unit = MetricUnits.SECONDS, tags = {"interface=ShareResource"})
  @Bulkhead(value = 5)
  public Response findShare(@Parameter(description = "Stock symbol, see [quote.cnbc.com](https://quote.cnbc.com)",
      example = "BMW.DE") @PathParam("key") @MDCKey(MDC_KEY) String key) {
    var share = shareSubscription.find(key);
    if (share.isPresent()) {
      return Response.ok(addLinks(uriInfo.getBaseUriBuilder(), share.get())).build();
    } else {
      throw new NotFoundException();
    }
  }

  @POST
  @Formatted
  @Operation(description = "Add a share to your list of subscriptions", operationId = "addShare")
  @Parameters({ @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_CALLER_ID),
          @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_REQUEST_ID) })
  @APIResponse(responseCode = "201", description = "Share queued for subscription",
          content = @Content(schema = @Schema(implementation = Share.class)))
  @APIResponse(responseCode = "500", description = "Queuing failed")
  @APIResponse(responseCode = "400", description = "Invalid data",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @SimplyTimed(name = "add-share", absolute = true, unit = MetricUnits.SECONDS, tags = {"interface=ShareResource"})
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
        MDC.put(MDC_KEY, share.getKey());
        log.debug("message sent: {}", message);
      }
      var location = UriBuilder.fromPath("shares/{0}").build(share.getKey());
      return Response.created(location).build();
    } catch (JMSException e) {
      log.error(e.getErrorCode(), e);
      return Response.status(INTERNAL_SERVER_ERROR).build();
    } finally {
      MDC.remove(CORRELATION_ID);
      MDC.remove(MDC_KEY);
    }
  }

  @DELETE
  @Path("/{key}")
  @Formatted
  @Operation(description = "Remove a subscription of a share")
  @Parameters({ @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_CALLER_ID),
          @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_REQUEST_ID) })
  @APIResponse(responseCode = "200", description = "Subscription removed",
      content = @Content(schema = @Schema(implementation = Share.class)))
  @APIResponse(responseCode = "404", description = "Subscription was not found")
  @SimplyTimed(name = "remove-share", absolute = true, unit = MetricUnits.SECONDS, tags = {"interface=ShareResource"})
  public Response removeShare(
      @Parameter(description = "Stock symbol", example = "GOOG") 
      @PathParam("key") @MDCKey(MDC_KEY) String key) {
    var share = shareSubscription.find(key);
    if (share.isPresent()) {
      shareSubscription.unsubscribe(share.get());
      return Response.ok(share.get()).build();
    } else {
      return Response.status(NOT_FOUND).build();
    }
  }

  private Share addLinks(UriBuilder baseUriBuilder, Share share) {
    var self = Link.fromUriBuilder(baseUriBuilder.clone().path(PATH).path(share.getKey())).rel(LinkConstants.REL_SELF)
        .build();
    var delete = Link.fromUriBuilder(baseUriBuilder.clone().path(PATH).path(share.getKey())).rel("unsubscribe")
        .param(LinkConstants.PARAM_METHOD, HttpMethod.DELETE).build();
    var quote = Link.fromUriBuilder(baseUriBuilder.clone().path(QuoteResource.PATH).path(share.getKey()))
        .rel("quote").build();
    share.setLinks(Arrays.asList(self, delete, quote));
    return share;
  }
}
