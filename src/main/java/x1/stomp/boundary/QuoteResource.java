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
import org.slf4j.Logger;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.QuoteRetriever;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Quote;
import x1.stomp.model.QuoteWrapper;
import x1.stomp.model.Quotes;
import static x1.stomp.model.Quotes.from;
import x1.stomp.model.Share;
import x1.stomp.util.Logged;
import x1.stomp.util.MDCKey;
import x1.stomp.version.VersionData;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import static javax.ws.rs.core.MediaType.*;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Bulkhead;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.opentracing.Traced;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;
import static x1.service.registry.Protocol.HTTP;
import static x1.service.registry.Protocol.HTTPS;
import static x1.service.registry.Technology.REST;

@Path(QuoteResource.PATH)
@RequestScoped
@Services(services = { @Service(technology = REST, value = RestApplication.ROOT + QuoteResource.PATH,
    version = VersionData.APP_VERSION_MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
@Transactional(Transactional.TxType.REQUIRES_NEW)
@Logged
@Traced
@Tag(name = "Quotes", description = "receive quotes for shares")
@Produces({ APPLICATION_JSON, APPLICATION_XML })
@Consumes({ APPLICATION_JSON, APPLICATION_XML })
@RestRequestStatusCounted
public class QuoteResource {
  protected static final String PATH = "/quotes";
  private static final String MDC_KEY = "quote";

  @Inject
  private Logger log;

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteRetriever quoteRetriever;

  @Resource
  private ManagedExecutorService mes;

  @Inject
  @ConfigProperty(name = "x1.stomp.boundary.QuoteResource/timeout", defaultValue = "5")
  private Integer timeout;

  @Context
  private UriInfo uriInfo;

  @GET
  @Path("/{key}")
  @Formatted
  @Operation(description = "get a quote")
  @Parameters({ @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_CALLER_ID),
    @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_REQUEST_ID) })
  @APIResponse(responseCode = "200", description = "Quote received",
      content = @Content(schema = @Schema(implementation = Quote.class)))
  @APIResponse(responseCode = "404", description = "Subscription not found")
  @SimplyTimed(name = "get-quote", absolute = true, unit = MetricUnits.SECONDS, tags = {"interface=QuoteResource"})
  @Bulkhead(value = 5)
  public Response getQuote(@Parameter(description = "Stock symbol, see [quote.cnbc.com](https://quote.cnbc.com)",
      example = "BMW.DE") @PathParam("key") @MDCKey(MDC_KEY) String key) {
    var share = shareSubscription.find(key);
    if (share.isPresent()) {
      var quote = quoteRetriever.retrieveQuote(share.get());
      if (quote.isPresent()) {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return Response.ok(addLinks(baseUriBuilder, quote.get())).build();
      }
    }
    return Response.status(NOT_FOUND).build();
  }

  @GET
  @Path("/")
  @Formatted
  @Operation(description = "get quotes")
  @Parameters({ @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_CALLER_ID),
          @Parameter(in = ParameterIn.HEADER, name = MDCFilter.X_REQUEST_ID) })
  @APIResponse(responseCode = "200", description = "Quotes received",
      content = {
          @Content(schema = @Schema(implementation = QuoteWrapper.class), mediaType = APPLICATION_XML),
          @Content(schema = @Schema(type=SchemaType.ARRAY, implementation = Quote.class),
              mediaType = APPLICATION_JSON) })
  @APIResponse(responseCode = "404", description = "No subscription found")
  @SimplyTimed(name = "get-quotes", absolute = true, unit = MetricUnits.SECONDS, tags = {"interface=QuoteResource"})
  @Bulkhead(value = 5)
  public void getQuotes(
      @Parameter(description = "Stock symbols", example = "[\"GOOG\"]") @QueryParam("key") @MDCKey(MDC_KEY) List<String> keys,
      @Suspended AsyncResponse response) {
    var baseUriBuilder = uriInfo.getBaseUriBuilder();
    withTimeoutHandler(response).execute(() -> response.resume(retrieveQuotes(keys, baseUriBuilder)));
  }

  private Response retrieveQuotes(List<String> keys, UriBuilder baseUriBuilder) {
    try {
      List<Share> shares;
      if (keys.isEmpty()) {
        shares = shareSubscription.list();
      } else {
        shares = keys.stream().map(key -> shareSubscription.find(key)).filter(Optional::isPresent).map(Optional::get)
            .collect(Collectors.toList());
      }
      if (shares.isEmpty()) {
        return Response.status(NOT_FOUND).entity(new Quotes()).build();
      }
      var quotes = quoteRetriever.retrieveQuotes(shares);
      quotes.forEach(quote -> addLinks(baseUriBuilder, quote));
      return Response.ok(from(quotes)).build();
    } catch (RuntimeException e) {
      log.error(null, e);
      throw e;
    }
  }

  private ManagedExecutorService withTimeoutHandler(AsyncResponse response) {
    response.setTimeout(timeout, SECONDS);
    response.setTimeoutHandler(r -> r.resume(Response.status(SERVICE_UNAVAILABLE).build()));
    return mes;
  }

  private Quote addLinks(UriBuilder baseUriBuilder, Quote quote) {
    var self = Link.fromUriBuilder(baseUriBuilder.clone().path(PATH).path(quote.getShare().getKey()))
        .rel(LinkConstants.REL_SELF).build();
    var share = Link.fromUriBuilder(baseUriBuilder.clone().path(ShareResource.PATH).path(quote.getShare().getKey()))
        .rel("parent").build();
    quote.setLinks(asList(self, share));
    return quote;
  }
}
