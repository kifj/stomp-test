package x1.stomp.boundary;

import java.util.concurrent.CountDownLatch;
import java.util.jar.Manifest;
  
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.eclipse.microprofile.openapi.OASFactory;
import org.eclipse.microprofile.openapi.OASModelReader;
import org.eclipse.microprofile.openapi.models.OpenAPI;

@WebListener
public class OpenAPIModelBuilder implements OASModelReader, ServletContextListener {
  private static final CountDownLatch LOCK = new CountDownLatch(1);
  private static ServletContext servletContext;
  
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    servletContext = sce.getServletContext();
    LOCK.countDown();
  }
  
  @Override
  public OpenAPI buildModel() {
    var openAPI =  OASFactory.createOpenAPI();
	
    // read info from manifest
    try {
      var manifest = new Manifest(servletContext.getResourceAsStream("/META-INF/MANIFEST.MF"));
      var attributes = manifest.getAttributes("Application");
      
      var info = OASFactory.createInfo().title(attributes.getValue("Application-Title"))
          .description(attributes.getValue("Application-Description"))
          .version(attributes.getValue("Application-Version"))
          .contact(OASFactory.createContact().email(attributes.getValue("Application-Contact")))
          .license(OASFactory.createLicense().name(attributes.getValue("Application-License"))
              .url(attributes.getValue("Application-LicenseUrl")));
      openAPI.info(info);
    } catch (Exception e) {
      // ignore
    }

    try {
      LOCK.await();
    } catch (InterruptedException e1) {
     return openAPI;
    }

    // add server by a relative url: works only if using swagger UI deployed with
    // the application
    var server = OASFactory.createServer().description("stage").url(servletContext.getContextPath());
    openAPI.addServer(server);
    return openAPI;
  }

}
