package x1.stomp.rest;

import java.util.ArrayList;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.Application;

import scala.collection.immutable.List;

import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.ApiInfo;
import com.wordnik.swagger.reader.ClassReaders;

@WebServlet(name = "SwaggerServlet", loadOnStartup = 1)
public class SwaggerServlet extends HttpServlet {
  private static final long serialVersionUID = 524694602295387341L;

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    // TODO read from POM
    SwaggerConfig swaggerConfig = new SwaggerConfig();
    ConfigFactory.setConfig(swaggerConfig);
    swaggerConfig.setBasePath("http://localhost:8080/stomp-test/rest");
    swaggerConfig.setApiVersion("1.2.0");

    ApiInfo info = new ApiInfo(
    /* title */
    "Stomp Test",
    /* description */
    "A test run for Stomp on JBoss Wildfly",
    /* TOS URL */
    null,
    /* Contact */
    "mail@johannes-beck.name",
    /* license */
    "The Apache Software License, Version 2.0",
    /* license URL */
    "http://www.apache.org/licenses/LICENSE-2.0.txt");
    swaggerConfig.setApiInfo(info);

    ScannerFactory.setScanner(new DefaultJaxrsScanner() {
      @Override
      public List<Class<?>> classesFromContext(Application application, ServletConfig config) {
        java.util.List<Class<?>> resources = new ArrayList<Class<?>>();

        resources.add(ShareResource.class);

        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResource.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class);
        resources.add(com.wordnik.swagger.jaxrs.listing.ResourceListingProvider.class);
        return scala.collection.JavaConversions.collectionAsScalaIterable(resources).toList();
      }
    });
    ClassReaders.setReader(new DefaultJaxrsApiReader());
  }
}
