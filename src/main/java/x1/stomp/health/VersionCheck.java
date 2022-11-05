package x1.stomp.health;

import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import x1.stomp.version.VersionData;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Readiness
public class VersionCheck implements HealthCheck {
  @Override
  public HealthCheckResponse call() {
    return HealthCheckResponse.named(x1.stomp.version.VersionData.APP_NAME)
        .withData("version", VersionData.APP_VERSION_MAJOR_MINOR).up().build();
  }
}
