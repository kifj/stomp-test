package x1.stomp.rest;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
  @Inject
  private Logger log;

  @Override
  public Response toResponse(ConstraintViolationException e) {
    ErrorResponse response = new ErrorResponse();
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      response.add(new ErrorMessage(violation.getMessage(), violation.getPropertyPath().toString(), violation
          .getInvalidValue()));
    }
    log.warn(response.toString());
    return Response.status(Status.PRECONDITION_FAILED).entity(response).build();
  }
}