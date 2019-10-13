package x1.stomp.boundary;

import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {
  @Inject
  private Logger log;

  @Context
  private UriInfo uriInfo;
  
  @Override
  public Response toResponse(ResteasyViolationException e) {
    ErrorResponse response = new ErrorResponse();
    response.setType("Invalid data");
    response.setRequestUri(uriInfo.getRequestUri().toString());
    for (ResteasyConstraintViolation violation : e.getViolations()) {
      response.add(new ErrorMessage(violation.getMessage(), violation.getPath(), violation.getValue()));
    }
    log.warn("Request failed because of invalid parameters:\n{}", response.toString());
    return Response.status(BAD_REQUEST).entity(response).build();
  }
}
