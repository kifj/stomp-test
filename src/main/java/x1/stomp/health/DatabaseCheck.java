package x1.stomp.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Readiness;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
@Readiness
public class DatabaseCheck implements HealthCheck {

  @Inject
  private EntityManager em;

  @Override
  public HealthCheckResponse call() {
    try {
      var count = em.createNamedQuery("Share.count", Long.class).getSingleResult();
      return HealthCheckResponse.named("database").withData("shares", count).up().build();
    } catch (Exception e) {
      return HealthCheckResponse.named("database").withData("error", e.getMessage()).down().build();
    }
  }
}
