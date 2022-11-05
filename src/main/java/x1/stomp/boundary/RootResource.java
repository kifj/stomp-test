package x1.stomp.boundary;

import static jakarta.ws.rs.HttpMethod.DELETE;
import static jakarta.ws.rs.HttpMethod.GET;
import static jakarta.ws.rs.HttpMethod.OPTIONS;
import static jakarta.ws.rs.HttpMethod.POST;
import static jakarta.ws.rs.HttpMethod.PUT;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.MediaType.APPLICATION_XML;
import static jakarta.ws.rs.core.MediaType.TEXT_HTML;
import static org.jboss.resteasy.spi.CorsHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
import static org.jboss.resteasy.spi.CorsHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;

import java.util.Arrays;

import jakarta.servlet.ServletContext;
import jakarta.websocket.server.PathParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.jboss.resteasy.annotations.providers.jaxb.Formatted;

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
