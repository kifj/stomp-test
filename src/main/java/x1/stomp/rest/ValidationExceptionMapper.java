package x1.stomp.rest;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.api.validation.ResteasyConstraintViolation;
import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.slf4j.Logger;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ResteasyViolationException> {
	@Inject
	private Logger log;

	@Override
	public Response toResponse(ResteasyViolationException e) {
		ErrorResponse response = new ErrorResponse();
		for (ResteasyConstraintViolation violation : e.getViolations()) {
			response.add(new ErrorMessage(violation.getMessage(), violation.getPath().toString(), violation.getValue()));
		}
		log.warn(response.toString());
		return Response.status(Status.PRECONDITION_FAILED).entity(response).build();
	}
}