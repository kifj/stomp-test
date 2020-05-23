package x1.stomp.boundary;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
public class RuntimeExceptionMapper implements ExceptionMapper<RuntimeException> {
  private static final MediaType XML_TYPE = MediaType.valueOf("*/xml");

  @Context
  private UriInfo uriInfo;

  @Context
  private HttpHeaders headers;
  
  @Override
  public Response toResponse(RuntimeException e) {
    ErrorResponse response = new ErrorResponse();
    response.setType(e.getClass().getSimpleName());
    response.setRequestUri(uriInfo.getRequestUri().toString());
    response.add(new ErrorMessage(e.getMessage()));
    MediaType type = MediaType.APPLICATION_JSON_TYPE;
    for (MediaType accepted : headers.getAcceptableMediaTypes()) {
      if (accepted.isCompatible(XML_TYPE)) {
        type = MediaType.APPLICATION_XML_TYPE;
      }
    }
    return Response.status(INTERNAL_SERVER_ERROR).entity(response).type(type).build();
  }
}
