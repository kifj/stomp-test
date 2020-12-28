package x1.stomp.control;

import javax.ws.rs.*;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import static x1.stomp.boundary.MDCFilter.HTTP_STATUS_CODE;;

public class QuickQuoteResponseExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {
  private static final Logger LOG = LoggerFactory.getLogger(QuickQuoteResponseExceptionMapper.class);

  @Override
  public boolean handles(int statusCode, MultivaluedMap<String, Object> headers) {
    return statusCode != Status.OK.getStatusCode();
  }

  @Override
  public WebApplicationException toThrowable(Response response) {
    WebApplicationException e;
    MDC.put(HTTP_STATUS_CODE, Integer.toString(response.getStatus()));
    if (response.getStatus() < Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
      // all 4xx errors are mapped to ClientErrorException and these are skipped in the circuit breaker
      e = new ClientErrorException(response);
      LOG.warn(e.getMessage());
    } else {
      // all 5xx are treated as server errors
      e = new ServerErrorException(response);
      LOG.error(e.getMessage());
    }
    MDC.remove(HTTP_STATUS_CODE);
    throw e;
  }
}
