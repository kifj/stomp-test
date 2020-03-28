package x1.stomp.boundary;

import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.websocket.server.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HttpMethod;

import static javax.ws.rs.HttpMethod.*;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

import javax.ws.rs.core.UriInfo;

import static org.jboss.resteasy.spi.CorsHeaders.*;

@Path(RootResource.PATH)
public class RootResource {
  protected static final String PATH = "/";

  @Context
  private UriInfo uriInfo;

  @Context
  private ServletContext context;

  @GET
  @Operation(description = "Link to available resources")
  @APIResponse(responseCode = "200", description = "The root resource",
    content = @Content(schema = @Schema(implementation = IndexResponse.class)))
  @Produces({ APPLICATION_JSON, APPLICATION_XML })
  @Formatted
  public Response index() {
    var self = Link.fromUriBuilder(uriInfo.getRequestUriBuilder()).rel(LinkConstants.REL_SELF).build();
    var swagger = Link
        .fromUriBuilder(uriInfo.getRequestUriBuilder().replacePath(context.getContextPath()).path("swagger"))
        .rel("documentation").type(TEXT_HTML).build();
    var quotes = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(QuoteResource.PATH)).rel("quotes").build();
    var shares = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(ShareResource.PATH)).rel("shares").build();
    var subscribe = Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(ShareResource.PATH)).rel("subscribe")
        .param(LinkConstants.PARAM_METHOD, HttpMethod.POST).build();

    return Response.ok(new IndexResponse(Arrays.asList(self, swagger, quotes, shares, subscribe))).build();
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
