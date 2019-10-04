package x1.stomp.health;

import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import x1.stomp.util.VersionData;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Liveness
public class VersionCheck implements HealthCheck {
  @Override
  public HealthCheckResponse call() {
    return HealthCheckResponse.named(VersionData.APP_NAME).withData("version", VersionData.MAJOR_MINOR).up().build();
  }
}
