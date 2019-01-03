package x1.stomp.control;

import javax.enterprise.context.Dependent;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;

@Produces(APPLICATION_JSON)
@RegisterProvider(QuickQuoteResponseExceptionMapper.class)
@Dependent
public interface QuickQuoteService {
  @GET
  @Path("/quote.htm")
  Response retrieve(@QueryParam("symbols") String symbols, @QueryParam("output") String output);
}
