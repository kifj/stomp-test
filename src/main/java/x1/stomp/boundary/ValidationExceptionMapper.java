package x1.stomp.boundary;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<ConstraintViolationException> {
  
  @Override
  public Response toResponse(ConstraintViolationException e) {
    var response = new ErrorResponse("Invalid data");
    var status = BAD_REQUEST;
    response.setRequestUri(uriInfo.getRequestUri().toString());
    e.getConstraintViolations().forEach(violation -> response.add(
        new ErrorMessage(violation.getMessage(), violation.getPropertyPath().toString(), violation.getInvalidValue())));
    info(status, "Request failed because of invalid parameters:\n{}", response.toString());
    return Response.status(status).entity(response).build();
  }
}
