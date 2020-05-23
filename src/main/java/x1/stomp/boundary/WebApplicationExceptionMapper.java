package x1.stomp.boundary;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

@Provider
public class WebApplicationExceptionMapper implements ExceptionMapper<WebApplicationException> {
  private static final MediaType XML_TYPE = MediaType.valueOf("*/xml");

  @Context
  private UriInfo uriInfo;

  @Context
  private HttpHeaders headers;
  
  @Inject
  private Logger log;
  
  @Override
  public Response toResponse(WebApplicationException e) {
    ErrorResponse response = new ErrorResponse();
    response.setRequestUri(uriInfo.getRequestUri().toString());
    response.setType(e.getResponse().getStatusInfo().getReasonPhrase());
    response.add(new ErrorMessage(e.getMessage()));
    log.warn(response.toString());
    MediaType type = MediaType.APPLICATION_JSON_TYPE;
    for (MediaType accepted : headers.getAcceptableMediaTypes()) {
      if (accepted.isCompatible(XML_TYPE)) {
        type = MediaType.APPLICATION_XML_TYPE;
      }
    }
    return Response.status(e.getResponse().getStatus()).type(type).entity(response).build();
  }
}
