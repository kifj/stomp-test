package x1.stomp.boundary;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Context;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
  @Context
  private UriInfo uriInfo;
  
  @Override
  public Response toResponse(RuntimeException e) {
    var response = new ErrorResponse();
    response.setType(e.getClass().getName());
    response.setRequestUri(uriInfo.getRequestUri().toString());
    response.add(new ErrorMessage(e.getMessage()));
    return Response.status(INTERNAL_SERVER_ERROR).entity(response).build();
  }
}
