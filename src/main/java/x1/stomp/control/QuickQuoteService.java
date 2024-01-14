package x1.stomp.control;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.*;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.time.temporal.ChronoUnit;

@Produces({ APPLICATION_JSON, APPLICATION_XML })
@RegisterProvider(QuickQuoteResponseExceptionMapper.class)
@RegisterProvider(BasicAuthFilter.class)
@RegisterRestClient
@Dependent
@Timeout(value = 2, unit = ChronoUnit.SECONDS)
@CircuitBreaker(failOn = { Exception.class }, skipOn = { ClientErrorException.class })
public interface QuickQuoteService {
  @GET
  @Path("/quote.htm")
  @WithSpan
  Response retrieve(@QueryParam("symbols") String symbols, @QueryParam("output") String output);
}
