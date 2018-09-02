package x1.stomp.boundary;

import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {
  @Inject
  private Logger log;

  @Override
  public Response toResponse(ResteasyViolationException e) {
    var response = new ErrorResponse();
    e.getViolations().forEach(violation -> response.add(new ErrorMessage(violation.getMessage(), violation.getPath(), violation.getValue())));
    log.warn(response.toString());
    return Response.status(PRECONDITION_FAILED).entity(response).build();
  }
}