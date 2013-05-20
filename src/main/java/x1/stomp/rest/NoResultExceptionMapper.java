package x1.stomp.rest;

import java.util.Arrays;

import javax.persistence.NoResultException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NoResultExceptionMapper implements ExceptionMapper<NoResultException> {

  @Override
  public Response toResponse(NoResultException e) {
    ErrorResponse response = new ErrorResponse();
    response.setErrors(Arrays.asList(new ErrorMessage(e.getMessage())));
    return Response.status(Status.NOT_FOUND).entity(response).build();
  }
}