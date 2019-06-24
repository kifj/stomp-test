package x1.stomp.boundary;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {

  @Override
  public Response toResponse(RuntimeException e) {
    var response = new ErrorResponse();
    response.add(new ErrorMessage(e.getMessage()));
    return Response.status(INTERNAL_SERVER_ERROR).entity(response).build();
  }
}
