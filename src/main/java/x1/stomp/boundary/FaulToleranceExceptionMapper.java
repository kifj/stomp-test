package x1.stomp.boundary;

import org.eclipse.microprofile.faulttolerance.exceptions.FaultToleranceException;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

@Provider
public class FaulToleranceExceptionMapper implements ExceptionMapper<FaultToleranceException> {
  @Inject
  private Logger log;

  @Context
  private UriInfo uriInfo;
  
  @Override
  public Response toResponse(FaultToleranceException e) {
    var response = new ErrorResponse();
    response.setType(e.getClass().getSimpleName());
    response.setRequestUri(uriInfo.getRequestUri().toString());
    log.warn("Service not available due to:\n{}", response.toString());
    return Response.status(SERVICE_UNAVAILABLE).entity(response).build();
  }
}
