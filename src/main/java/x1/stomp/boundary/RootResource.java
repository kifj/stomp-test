package x1.stomp.boundary;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import static javax.ws.rs.HttpMethod.*;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import static org.jboss.resteasy.spi.CorsHeaders.*;

import io.swagger.v3.oas.annotations.Operation;

@Path(RootResource.PATH)
public class RootResource {
  protected static final String PATH = "/";

  @Context
  private UriInfo uriInfo;

  @Context
  private ServletContext context;

  @GET
  @Operation(description = "Link to available resources")
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  public Response index() {
    var self = Link.fromUriBuilder(uriInfo.getRequestUriBuilder()).rel("self").build();
    var swagger = Link
        .fromUriBuilder(uriInfo.getRequestUriBuilder().replacePath(context.getContextPath()).path("swagger"))
        .rel("documentation").type(TEXT_HTML).build();
    var quotes = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(QuoteResource.PATH)).rel("quotes").build();
    var shares = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(ShareResource.PATH)).rel("shares").build();

    return Response.ok(new IndexResponse(Arrays.asList(self, swagger, quotes, shares))).build();
  }

  @GET
  @Operation(description = "Link to Swagger UI")
  @Produces(TEXT_HTML)
  public Response swagger() {
    var uri = uriInfo.getRequestUriBuilder().replacePath(context.getContextPath()).path("swagger").build();
    return Response.status(Status.MOVED_PERMANENTLY).location(uri).build();
  }

  @OPTIONS
  @Consumes("*/*")
  @Path("{path:.*}")
  public Response options(@PathParam("path") String path) {
    return Response.ok().allow(GET, PUT, POST, DELETE, OPTIONS).header(ACCESS_CONTROL_ALLOW_CREDENTIALS, true)
        .header(ACCESS_CONTROL_ALLOW_ORIGIN, "*").build();
  }

}
