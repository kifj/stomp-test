package x1.stomp.boundary;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

@ApplicationPath(RestApplication.ROOT)
public class RestApplication extends Application {
  public static final String ROOT = "/rest";
}
