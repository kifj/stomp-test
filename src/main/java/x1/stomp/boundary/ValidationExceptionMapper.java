package x1.stomp.boundary;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper extends ExceptionMapperBase implements ExceptionMapper<ConstraintViolationException> {
  
  @Override
  public Response toResponse(ConstraintViolationException e) {
    var response = new ErrorResponse("Invalid data");
    var status = BAD_REQUEST;
    response.setRequestUri(uriInfo.getRequestUri().toString());
    e.getConstraintViolations().forEach(violation -> response.add(ErrorMessage.of(violation)));
    info(status, "Request failed because of invalid parameters:\n{}", response.toString());
    return Response.status(status).entity(response).build();
  }
}
