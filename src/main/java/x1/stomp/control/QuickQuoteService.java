package x1.stomp.control;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.*;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;
import org.eclipse.microprofile.opentracing.Traced;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;

@Produces({ APPLICATION_JSON, APPLICATION_XML })
@RegisterProvider(QuickQuoteResponseExceptionMapper.class)
@RegisterProvider(BasicAuthFilter.class)
@RegisterRestClient
@Dependent
@Timeout(value = 2, unit = ChronoUnit.SECONDS)
@CircuitBreaker(failOn = { Exception.class }, skipOn = { ClientErrorException.class })
@Traced
public interface QuickQuoteService {
  @GET
  @Path("/quote.htm")
  @SimplyTimed(name = "retrieve-quickquote", absolute = true, unit = MetricUnits.SECONDS,
      tags = { "interface=QuickQuoteService" })
  Response retrieve(@QueryParam("symbols") String symbols, @QueryParam("output") String output);
}
