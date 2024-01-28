package x1.stomp.boundary;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import static x1.service.registry.Protocol.HTTP;
import static x1.service.registry.Protocol.HTTPS;
import static x1.service.registry.Technology.REST;
import static x1.stomp.boundary.MDCFilter.X_CALLER_ID;
import static x1.stomp.boundary.MDCFilter.X_REQUEST_ID;
import static x1.stomp.boundary.LinkConstants.*;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.Queue;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.ParameterIn;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameters;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.slf4j.Logger;
import org.slf4j.MDC;

import io.micrometer.core.annotation.Timed;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Action;
import x1.stomp.model.Share;
import x1.stomp.model.ShareWrapper;
import x1.stomp.util.Logged;
import x1.stomp.util.MDCKey;
import x1.stomp.util.Metered;
import x1.stomp.util.StockMarket;
import x1.stomp.version.VersionData;

@Path(ShareResource.PATH)
@Produces({ APPLICATION_JSON, APPLICATION_XML })
@Consumes({ APPLICATION_JSON, APPLICATION_XML })
@RequestScoped
@Services(services = { @Service(technology = REST, value = RestApplication.ROOT + ShareResource.PATH,
    version = VersionData.APP_VERSION_MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
@Transactional
@Logged
@Metered
@Timeout(value = 5, unit = ChronoUnit.SECONDS)
@Tag(name = "Shares", description = "subscribe to shares on the stock market")
@RestRequestStatusCounted
public class ShareResource {
  private static final String MDC_KEY = "share";
  private static final String CORRELATION_ID = "correlationId";
  protected static final String PATH = "/shares";

  @Inject
  private Logger log;

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  @JMSConnectionFactory("java:/JmsXA")
  private JMSContext context;

  @Inject
  @StockMarket
  private Queue stockMarketQueue;

  @Context
  private UriInfo uriInfo;

  @GET
  @Wrapped(element = "shares")
  @Formatted
  @Operation(description = "List all subscriptions")
  @Parameters({
      @Parameter(in = ParameterIn.HEADER, name = X_CALLER_ID, schema = @Schema(implementation = String.class)),
      @Parameter(in = ParameterIn.HEADER, name = X_REQUEST_ID, schema = @Schema(implementation = String.class)) })
  @APIResponse(responseCode = "200", description = "All subscriptions",
      content = {
          @Content(schema = @Schema(type = SchemaType.ARRAY, implementation = Share.class),
              mediaType = APPLICATION_JSON),
          @Content(schema = @Schema(implementation = ShareWrapper.class), mediaType = APPLICATION_XML) })
  @Timed
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
  @Parameters({
      @Parameter(in = ParameterIn.HEADER, name = X_CALLER_ID, schema = @Schema(implementation = String.class)),
      @Parameter(in = ParameterIn.HEADER, name = X_REQUEST_ID, schema = @Schema(implementation = String.class)) })
  @APIResponse(responseCode = "200", description = "Subscription found",
      content = @Content(schema = @Schema(implementation = Share.class)))
  @APIResponse(responseCode = "404", description = "Subscription not found")
  @Timed
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
  @Parameters({
      @Parameter(in = ParameterIn.HEADER, name = X_CALLER_ID, schema = @Schema(implementation = String.class)),
      @Parameter(in = ParameterIn.HEADER, name = X_REQUEST_ID, schema = @Schema(implementation = String.class)) })
  @APIResponse(responseCode = "201", description = "Share queued for subscription",
      content = @Content(schema = @Schema(implementation = Share.class)))
  @APIResponse(responseCode = "500", description = "Queuing failed")
  @APIResponse(responseCode = "400", description = "Invalid data",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  @Timed
  public Response addShare(
      @Parameter(required = true,
          description = "The share which is will be added for subscription") @NotNull @Valid Share share,
      @Parameter(
          description = "provide a Correlation-Id header to receive a response for your operation when it finished.") 
      @HeaderParam(value = "Correlation-Id") String correlationId) {
    try {
      var jmsCorrelationId = Objects.requireNonNullElse(correlationId, UUID.randomUUID().toString());
      context.createProducer().setJMSCorrelationID(jmsCorrelationId).setProperty("type", "share")
          .setProperty("action", Action.SUBSCRIBE.name()).send(stockMarketQueue, share);
      MDC.put(CORRELATION_ID, jmsCorrelationId);
      MDC.put(MDC_KEY, share.getKey());
      log.debug("message sent: {}", share);
      var location = UriBuilder.fromResource(ShareResource.class).path("{0}").build(share.getKey());
      return Response.created(location).build();
    } finally {
      MDC.remove(CORRELATION_ID);
      MDC.remove(MDC_KEY);
    }
  }

  @DELETE
  @Path("/{key}")
  @Formatted
  @Operation(description = "Remove a subscription of a share")
  @Parameters({
      @Parameter(in = ParameterIn.HEADER, name = X_CALLER_ID, schema = @Schema(implementation = String.class)),
      @Parameter(in = ParameterIn.HEADER, name = X_REQUEST_ID, schema = @Schema(implementation = String.class)) })
  @APIResponse(responseCode = "200", description = "Subscription removed",
      content = @Content(schema = @Schema(implementation = Share.class)))
  @APIResponse(responseCode = "404", description = "Subscription was not found")
  @Timed
  public Response removeShare(
      @Parameter(description = "Stock symbol", example = "GOOG") @PathParam("key") @MDCKey(MDC_KEY) String key) {
    var candidate = shareSubscription.find(key);
    if (candidate.isPresent()) {
      var share = shareSubscription.unsubscribe(candidate.get());
      return Response.ok(share).build();
    } else {
      return Response.status(NOT_FOUND).build();
    }
  }

  private Share addLinks(UriBuilder baseUriBuilder, Share share) {
    var self = Link.fromUriBuilder(baseUriBuilder.clone().path(PATH).path(share.getKey())).rel(REL_SELF).build();
    var delete = Link.fromUriBuilder(baseUriBuilder.clone().path(PATH).path(share.getKey())).rel("unsubscribe")
        .param(PARAM_METHOD, HttpMethod.DELETE).build();
    var quote = Link.fromUriBuilder(baseUriBuilder.clone().path(QuoteResource.PATH).path(share.getKey())).rel("quote")
        .build();
    share.setLinks(Arrays.asList(self, delete, quote));
    return share;
  }
}
