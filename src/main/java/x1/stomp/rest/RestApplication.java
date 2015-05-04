package x1.stomp.rest;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/rest")
public class RestApplication extends Application {
  /*
   * TODO enable CorsFilter -> Add resources for Swagger in getClasses()
   * 
   * @Override public Set<Object> getSingletons() { CorsFilter filter = new
   * CorsFilter(); filter.getAllowedOrigins().add("*"); Set<Object> resources =
   * new HashSet<Object>(); resources.add(filter); return resources; }
   */
}
