package x1.stomp.boundary;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.slf4j.Logger;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.QuoteRetriever;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Quote;
import x1.stomp.model.Quotes;
import x1.stomp.model.Share;
import x1.stomp.util.VersionData;

import javax.annotation.Resource;
import javax.transaction.Transactional;
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
@Api(value = QuoteResource.PATH)
@Services(services = {@Service(technology = REST, value = RestApplication.ROOT
        + QuoteResource.PATH, version = VersionData.MAJOR_MINOR, protocols = {HTTP, HTTPS})})
@Transactional(Transactional.TxType.REQUIRES_NEW)
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

  @GET
  @Path("/{key}")
  @ApiOperation(value = "get a quote")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Quote received", response = Quote.class),
          @ApiResponse(code = 404, message = "Subscription not found")})
  @SimplyTimed(name = "get-quote", absolute = true, tags = {"interface=QuoteResource"})
  public Response getQuote(
          @ApiParam("Stock symbol (e.g. BMW.DE), see https://quote.cnbc.com") @PathParam("key") String key) {
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
  @ApiOperation(value = "get quotes")
  @ApiResponses(value = {@ApiResponse(code = 200, message = "Quotes received", response = Quote[].class),
          @ApiResponse(code = 404, message = "No subscription found")})
  @SimplyTimed(name = "get-quotes", absolute = true, tags = {"interface=QuoteResource"})
  public void getQuotes(@ApiParam("Stock symbols") @QueryParam("key") List<String> keys,
                        @Suspended AsyncResponse response) {
    withTimeoutHandler(response).execute(() -> response.resume(retrieveQuotes(keys)));
  }

  private Response retrieveQuotes(List<String> keys) {
    try {
      List<Share> shares = keys.stream().map(key -> shareSubscription.find(key)).filter(Optional::isPresent)
          .map(Optional::get).collect(Collectors.toList());
      if (shares.isEmpty()) {
        return Response.status(NOT_FOUND).entity(new Quotes()).build();
      }
      List<Quote> quotes = quoteRetriever.retrieveQuotes(shares);
      return Response.ok(new Quotes(quotes)).build();
    } catch (RuntimeException e) {
      log.error(null, e);
      throw e;
    }
  }

  private ManagedExecutorService withTimeoutHandler(AsyncResponse response) {
    response.setTimeout(5, SECONDS);
    response.setTimeoutHandler(r -> r.resume(Response.status(SERVICE_UNAVAILABLE).build()));
    return mes;
  }

}
