package x1.stomp.control;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickQuoteResponseExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {
  private static final Logger LOG = LoggerFactory.getLogger(QuickQuoteResponseExceptionMapper.class);

  @Override
  public boolean handles(int statusCode, MultivaluedMap<String, Object> headers) {
    return statusCode != Status.OK.getStatusCode();
  }

  @Override
  public WebApplicationException toThrowable(Response response) {
    var e = new WebApplicationException(response);
    LOG.error(e.getMessage());
    return e;
  }
}
