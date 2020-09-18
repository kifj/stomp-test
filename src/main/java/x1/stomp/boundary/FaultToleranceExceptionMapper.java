package x1.stomp.boundary;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;

import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

import javax.ws.rs.core.Context;

@Provider
public class FaultToleranceExceptionMapper implements ExceptionMapper<FaultToleranceException> {
  @Context
  private UriInfo uriInfo;
  
  @Override
  public Response toResponse(FaultToleranceException e) {
    var response = new ErrorResponse(e.getClass().getSimpleName());
    response.setRequestUri(uriInfo.getRequestUri().toString());
    response.add(new ErrorMessage(e.getMessage()));
    return Response.status(SERVICE_UNAVAILABLE).entity(response).build();
  }
}
