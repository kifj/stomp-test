package x1.stomp.boundary;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper extends ExceptionMapperBase
    implements ExceptionMapper<WebApplicationException> {
  @Context
  private UriInfo uriInfo;

  @Override
  public Response toResponse(WebApplicationException e) {
    var response = new ErrorResponse(e.getResponse().getStatusInfo().getReasonPhrase());
    response.setRequestUri(uriInfo.getRequestUri().toString());
    response.add(new ErrorMessage(e.getMessage()));
    warn(Status.fromStatusCode(e.getResponse().getStatus()), response.toString());
    return Response.status(e.getResponse().getStatus()).entity(response).build();
  }
}
