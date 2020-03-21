package x1.stomp.control;

import javax.enterprise.context.Dependent;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Produces(APPLICATION_JSON)
@RegisterProvider(QuickQuoteResponseExceptionMapper.class)
@RegisterRestClient
@Dependent
@Traced(value = true)
public interface QuickQuoteService {
  @GET
  @Path("/quote.htm")
  @Timed(name = "quickquote-retrieve", absolute = true, unit = MetricUnits.MILLISECONDS)
  Response retrieve(@QueryParam("symbols") String symbols, @QueryParam("output") String output);
}
