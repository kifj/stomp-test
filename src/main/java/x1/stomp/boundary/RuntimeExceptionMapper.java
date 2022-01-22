package x1.stomp.boundary;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RuntimeExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<RuntimeException> {
  
  @Override
  public Response toResponse(RuntimeException e) {
    // will be logged by LoggingInterceptor
    var response = new ErrorResponse(e.getClass().getSimpleName());
    response.setRequestUri(uriInfo.getRequestUri().toString());
    response.add(ErrorMessage.from(e));
    return Response.status(INTERNAL_SERVER_ERROR).entity(response).build();
  }
}
