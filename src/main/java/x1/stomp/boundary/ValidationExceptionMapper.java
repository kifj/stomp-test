package x1.stomp.boundary;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.PRECONDITION_FAILED;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
  @Inject
  private Logger log;

  @Override
  public Response toResponse(ConstraintViolationException e) {
    var response = new ErrorResponse();
    e.getConstraintViolations().forEach(violation -> response.add(
        new ErrorMessage(violation.getMessage(), violation.getPropertyPath().toString(), violation.getInvalidValue())));
    log.warn("Request failed because of invalid parameters:\n{}", response.toString());
    return Response.status(PRECONDITION_FAILED).entity(response).build();
  }
}
