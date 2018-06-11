package x1.stomp.boundary;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.QuoteRetriever;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Quote;
import x1.stomp.model.Quotes;
import x1.stomp.model.Share;
import x1.stomp.util.VersionData;

import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.config.inject.ConfigProperty;

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
@Services(services = { @Service(technology = REST, value = RestApplication.ROOT
    + QuoteResource.PATH, version = VersionData.MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class QuoteResource {
  protected static final String PATH = "/quotes";

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteRetriever quoteRetriever;

  @Resource
  private ManagedExecutorService mes;

  @Inject
  @ConfigProperty(name = "x1.stomp.boundary.QuoteResource/timeout", defaultValue = "5")
  private Integer timeout;

  @GET
  @Path("/{key}")
  @Operation(description = "get a quote")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Quote received", 
          content = @Content(schema = @Schema(implementation = Quote.class))),
      @ApiResponse(responseCode = "404", description = "Subscription not found") })
  public Response getQuote(
      @Parameter(description = "Stock symbol (e.g. BMW.DE), see https://quote.cnbc.com") @PathParam("key") String key) {
    Optional<Share> share = shareSubscription.find(key);
    if (share.isPresent()) {
      Optional<Quote> quote = quoteRetriever.retrieveQuote(share.get());
      if (quote.isPresent()) {
        return Response.ok(quote.get()).build();
      }
    }
    return Response.status(NOT_FOUND).build();
  }

  @GET
  @Path("/")
  @Operation(description = "get quotes")
  @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Quotes received", 
        content = @Content(schema = @Schema(implementation = Quote[].class))),
      @ApiResponse(responseCode = "404", description = "No subscription found") })
  public void getQuotes(@Parameter(description = "Stock symbols") @QueryParam("key") List<String> keys,
      @Suspended AsyncResponse response) {
    withTimeoutHandler(response).execute(() -> response.resume(retrieveQuotes(keys)));
  }

  private Response retrieveQuotes(List<String> keys) {
    List<Share> shares = keys.stream().map(key -> shareSubscription.find(key)).filter(Optional::isPresent)
        .map(Optional::get).collect(Collectors.toList());
    if (shares.isEmpty()) {
      return Response.status(NOT_FOUND).build();
    }
    List<Quote> quotes = quoteRetriever.retrieveQuotes(shares);
    return Response.ok(new Quotes(quotes)).build();
  }

  private ManagedExecutorService withTimeoutHandler(AsyncResponse response) {
    response.setTimeout(timeout, SECONDS);
    response.setTimeoutHandler(r -> r.resume(Response.status(SERVICE_UNAVAILABLE).build()));
    return mes;
  }

}
