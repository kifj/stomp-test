package x1.stomp.boundary;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
  @Inject
  private Logger log;

  @Context
  private UriInfo uriInfo;
  
  @Override
  public Response toResponse(ConstraintViolationException e) {
    var response = new ErrorResponse("Invalid data");
    response.setRequestUri(uriInfo.getRequestUri().toString());
    e.getConstraintViolations().forEach(violation -> response.add(
        new ErrorMessage(violation.getMessage(), violation.getPropertyPath().toString(), violation.getInvalidValue())));
    log.info("Request failed because of invalid parameters:\n{}", response.toString());
    return Response.status(BAD_REQUEST).entity(response).build();
  }
}
