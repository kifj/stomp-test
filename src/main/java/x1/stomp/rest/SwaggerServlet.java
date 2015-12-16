package x1.stomp.rest;

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

    //TODO hard-coded parameters
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1.3.0");
    beanConfig.setDescription("A test run for Stomp on JBoss Wildfly");
    beanConfig.setTitle("Stomp Test");
    beanConfig.setSchemes(new String[]{"http"});
    beanConfig.setHost("localhost:8080");
    beanConfig.setBasePath("/stomp-test-v1.3/rest");
    beanConfig.setResourcePackage("x1.stomp.rest");
    beanConfig.setContact("mail@johannes-beck.name");
    beanConfig.setLicense("The Apache Software License, Version 2.0");
    beanConfig.setLicenseUrl("http://www.apache.org/licenses/LICENSE-2.0.txt");
    beanConfig.setScan(true);    
  }
}
