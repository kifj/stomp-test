package x1.stomp.rest;

import static x1.service.registry.Protocol.HTTP;
import static x1.service.registry.Protocol.HTTPS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.service.registry.Technology;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.service.QuoteRetriever;
import x1.stomp.service.ShareSubscription;
import x1.stomp.util.VersionData;

@Path(QuoteResource.PATH)
@RequestScoped
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api(value = QuoteResource.PATH)
@Services(services = { @Service(technology = Technology.REST, value = RestApplication.ROOT
    + QuoteResource.PATH, version = VersionData.MAJOR_MINOR, protocols = { HTTP, HTTPS }) })
public class QuoteResource {
  public static final String PATH = "/quotes";

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteRetriever quoteRetriever;

  @Resource
  private ManagedExecutorService mes;

  @GET
  @Path("/{key}")
  @ApiOperation(value = "get a quote")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Quote received", response = Quote.class),
      @ApiResponse(code = 404, message = "Subscription not found") })
  public Response getQuote(
      @ApiParam("Stock symbol (e.g. BMW.DE), see https://quote.cnbc.com") @PathParam("key") String key) {
    Share share = shareSubscription.find(key);
    if (share != null) {
      Quote quote = quoteRetriever.retrieveQuote(share);
      if (quote != null) {
        return Response.ok(quote).build();
      }
    }
    return Response.status(Status.NOT_FOUND).build();
  }

  @GET
  @Path("/")
  @ApiOperation(value = "get quotes")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Quotes received", response = Quote[].class),
      @ApiResponse(code = 404, message = "Subscription not found") })
  public void getQuotes(@ApiParam("Stock symbols") @QueryParam("key") String[] keys,
      @Suspended AsyncResponse response) {
    withTimeoutHandler(response);
    mes.execute(() -> response.resume(retrieveQuotes(keys)));
  }

  private Response retrieveQuotes(String[] keys) {
    List<Share> shares = new ArrayList<>();
    for (String key : keys) {
      Share share = shareSubscription.find(key);
      if (share != null) {
        shares.add(share);
      }
    }
    if (shares.isEmpty()) {
      return Response.status(Status.NOT_FOUND).build();
    }
    List<Quote> quotes = quoteRetriever.retrieveQuotes(shares);
    return Response.ok(quotes).build();
  }

  private AsyncResponse withTimeoutHandler(AsyncResponse response) {
    response.setTimeout(5, TimeUnit.SECONDS);
    response.setTimeoutHandler(r -> r.resume(Response.status(Status.SERVICE_UNAVAILABLE).build()));
    return response;
  }

}
