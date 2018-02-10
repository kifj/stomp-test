package x1.stomp.boundary;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath(RestApplication.ROOT)
public class RestApplication extends Application {
  public static final String ROOT = "/rest";

}
