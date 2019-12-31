package x1.stomp.boundary;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.MDC;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
  @Context
  private UriInfo uriInfo;

  @Inject
  private Logger log;

  @Override
  public Response toResponse(WebApplicationException e) {
    try {
      MDC.put(MDCFilter.HTTP_STATUS_CODE, Integer.toString(e.getResponse().getStatus()));
      var response = new ErrorResponse();
      response.setRequestUri(uriInfo.getRequestUri().toString());
      response.setType(e.getResponse().getStatusInfo().getReasonPhrase());
      response.add(new ErrorMessage(e.getMessage()));
      log.warn(response.toString());
      return Response.status(e.getResponse().getStatus()).entity(response).build();
    } finally {
      MDC.remove(MDCFilter.HTTP_STATUS_CODE);
    }
  }
}
