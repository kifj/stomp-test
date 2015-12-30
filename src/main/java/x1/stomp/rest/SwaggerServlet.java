package x1.stomp.rest;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import io.swagger.jaxrs.config.BeanConfig;

@WebServlet(name = "SwaggerServlet", loadOnStartup = 1)
public class SwaggerServlet extends HttpServlet {
  private static final long serialVersionUID = 524694602295387341L;

  @Override
  public void init(ServletConfig servletConfig) throws ServletException {
    super.init(servletConfig);
    BeanConfig beanConfig = new BeanConfig();
    try {
      Manifest manifest = new Manifest(getServletContext().getResourceAsStream("/META-INF/MANIFEST.MF"));
      Attributes attributes = manifest.getAttributes("Application");
      beanConfig.setVersion(attributes.getValue("Application-Version"));
      beanConfig.setDescription(attributes.getValue("Application-Description"));
      beanConfig.setTitle(attributes.getValue("Application-Title"));
      beanConfig.setContact(attributes.getValue("Application-Contact"));
      beanConfig.setLicense(attributes.getValue("Application-License"));
      beanConfig.setLicenseUrl(attributes.getValue("Application-LicenseUrl"));
    } catch (IOException e) {
      // ignore
    }
    beanConfig.setSchemes(new String[] { "http" });
    //beanConfig.setHost("localhost:8080");
    beanConfig.setBasePath(servletConfig.getServletContext().getContextPath() + "/rest");
    beanConfig.setResourcePackage("x1.stomp.rest");
    beanConfig.setScan(true);
  }
}
