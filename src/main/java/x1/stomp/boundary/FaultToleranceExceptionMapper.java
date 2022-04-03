package x1.stomp.boundary;

import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;

@Provider
public class FaultToleranceExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<FaultToleranceException> {
  
  @Override
  public Response toResponse(FaultToleranceException e) {
    var response = new ErrorResponse(e.getClass().getSimpleName());
    var status = SERVICE_UNAVAILABLE;
    response.setRequestUri(uriInfo.getRequestUri().toString());
    response.add(ErrorMessage.from(e));
    warn(status, "Service not available due to:\n{}", response.toString());
    return Response.status(status).entity(response).build();
  }
}
