package x1.stomp.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.service.QuoteRetriever;
import x1.stomp.service.ShareSubscription;

@Path("/quotes")
@RequestScoped
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Api(value = "/quotes")
public class QuoteResource {

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteRetriever quoteRetriever;
  
  @GET
  @Path("/{key}")
  @ApiOperation(value = "get a quote")
  @ApiResponses(value = { @ApiResponse(code = 200, message = "Subscription found", response = Quote.class),
      @ApiResponse(code = 404, message = "Subscription not found") })
  public Response getQuote(
      @ApiParam("Stock symbol (e.g. BMW.DE), see http://finance.yahoo.com/q") @PathParam("key") String key) {
    Share share = shareSubscription.find(key);
    if (share != null) {
      Quote quote = quoteRetriever.retrieveQuote(share);
      if (quote != null) {
        return Response.ok(quote).build();
      }
    }
    return Response.status(Status.NOT_FOUND).build();
  }

}
