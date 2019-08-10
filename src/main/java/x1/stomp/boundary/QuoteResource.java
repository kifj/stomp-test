package x1.stomp.boundary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.QuoteRetriever;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Quote;
import x1.stomp.model.QuoteWrapper;
import x1.stomp.model.Quotes;
import x1.stomp.model.Share;
import x1.stomp.util.Logged;
import x1.stomp.util.VersionData;

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

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.annotation.Metered;
import org.eclipse.microprofile.opentracing.Traced;

import java.util.Arrays;
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
    version = VersionData.MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
@Transactional(Transactional.TxType.REQUIRES_NEW)
@Logged
@Traced
@Tag(name = "Quotes", description = "receive quotes for shares")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public class QuoteResource {
  protected static final String PATH = "/quotes";

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
  @Operation(description = "get a quote")
  @ApiResponse(responseCode = "200", description = "Quote received",
      content = @Content(schema = @Schema(implementation = Quote.class)))
  @ApiResponse(responseCode = "404", description = "Subscription not found")
  @Metered(name = "quote-meter", absolute = true)
  public Response getQuote(@Parameter(description = "Stock symbol, see [quote.cnbc.com](https://quote.cnbc.com)",
      example = "BMW.DE") @PathParam("key") String key) {
    Optional<Share> share = shareSubscription.find(key);
    if (share.isPresent()) {
      Optional<Quote> quote = quoteRetriever.retrieveQuote(share.get());
      if (quote.isPresent()) {
        UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
        return Response.ok(addLinks(baseUriBuilder, quote.get())).build();
      }
    }
    return Response.status(NOT_FOUND).build();
  }

  @GET
  @Path("/")
  @Operation(description = "get quotes")
  @ApiResponse(responseCode = "200", description = "Quotes received",
      content = {
          @Content(schema = @Schema(implementation = QuoteWrapper.class), mediaType = MediaType.APPLICATION_XML),
          @Content(array = @ArraySchema(schema = @Schema(implementation = Quote.class)),
              mediaType = MediaType.APPLICATION_JSON) })
  @ApiResponse(responseCode = "404", description = "No subscription found")
  @Metered(name = "quotes-meter", absolute = true)
  public void getQuotes(
      @Parameter(description = "Stock symbols", example = "[\"GOOG\"]") @QueryParam("key") List<String> keys,
      @Suspended AsyncResponse response) {
    UriBuilder baseUriBuilder = uriInfo.getBaseUriBuilder();
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
      List<Quote> quotes = quoteRetriever.retrieveQuotes(shares);
      quotes.forEach(quote -> addLinks(baseUriBuilder, quote));
      return Response.ok(new Quotes(quotes)).build();
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
    Link self = Link.fromUriBuilder(baseUriBuilder.clone().path(PATH).path(quote.getShare().getKey())).rel("self")
        .build();
    quote.setLinks(Arrays.asList(self));
    return quote;
  }
}
