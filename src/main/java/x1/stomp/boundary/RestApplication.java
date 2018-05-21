package x1.stomp.boundary;

import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletConfig;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@ApplicationPath(RestApplication.ROOT)
public class RestApplication extends Application {
  public static final String ROOT = "/rest";

  public RestApplication(@Context ServletConfig servletConfig) {
    var oas = new OpenAPI();

    // read info from manifest
    try {
      var manifest = new Manifest(servletConfig.getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
      var attributes = manifest.getAttributes("Application");

      var info = new Info().title(attributes.getValue("Application-Title"))
          .description(attributes.getValue("Application-Description"))
          .version(attributes.getValue("Application-Version"))
          .contact(new Contact().email(attributes.getValue("Application-Contact"))).license(new License()
              .name(attributes.getValue("Application-License")).url(attributes.getValue("Application-LicenseUrl")));
      oas.info(info);
    } catch (Exception e) {
      // ignore
    }

    // add server by a relative url: works only if using swagger UI deployed with
    // the application
    Server server = new Server().url(servletConfig.getServletContext().getContextPath());
    oas.addServersItem(server);

    var oasConfig = new SwaggerConfiguration().openAPI(oas).prettyPrint(true)
        .resourcePackages(Stream.of(this.getClass().getPackage().getName()).collect(Collectors.toSet()));

    try {
      new JaxrsOpenApiContextBuilder<>().servletConfig(servletConfig).application(this).openApiConfiguration(oasConfig)
          .buildContext(true);
    } catch (OpenApiConfigurationException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
