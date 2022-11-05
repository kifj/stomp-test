package x1.stomp.boundary;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WebApplicationExceptionMapper extends ExceptionMapperBase
    implements ExceptionMapper<WebApplicationException> {
  @Context
  private UriInfo uriInfo;

  @Override
  public Response toResponse(WebApplicationException e) {
    var response = new ErrorResponse(e.getResponse().getStatusInfo().getReasonPhrase());
    response.setRequestUri(uriInfo.getRequestUri().toString());
    response.add(ErrorMessage.from(e));
    warn(Status.fromStatusCode(e.getResponse().getStatus()), response.toString());
    return Response.status(e.getResponse().getStatus()).entity(response).build();
  }
}
