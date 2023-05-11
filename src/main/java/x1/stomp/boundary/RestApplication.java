package x1.stomp.boundary;

import java.util.Map;
import java.util.Objects;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import x1.stomp.version.VersionData;

@ApplicationPath(RestApplication.ROOT)
public class RestApplication extends Application {
  @Inject
  private MeterRegistry registry;

  public static final String ROOT = "/rest";

  @Override
  public Map<String, Object> getProperties() {
    registerCommonTags(registry);
    return super.getProperties();
  }

  private void registerCommonTags(MeterRegistry registry) {
    registry.config().commonTags("service_name",
        Objects.requireNonNullElse(System.getenv("SERVICE_NAME"), VersionData.APP_NAME), "version",
        VersionData.APP_VERSION_MAJOR_MINOR, "node_name",
        System.getProperty(" jboss.node.name", System.getProperty("jboss.host.name")));
  }
}
