package x1.stomp.health;

import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import x1.stomp.version.VersionData;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Liveness
public class VersionCheck implements HealthCheck {
  @Override
  public HealthCheckResponse call() {
    return HealthCheckResponse.named(x1.stomp.version.VersionData.APP_NAME)
        .withData("version", VersionData.APP_VERSION_MAJOR_MINOR).up().build();
  }
}
