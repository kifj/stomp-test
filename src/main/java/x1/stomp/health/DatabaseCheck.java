package x1.stomp.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import x1.stomp.model.Share;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
@Liveness
public class DatabaseCheck implements HealthCheck {

  @Inject
  private EntityManager em;

  @Override
  public HealthCheckResponse call() {
    try {
      var count = em.createNamedQuery(Share.COUNT_ALL, Long.class).getSingleResult();
      return HealthCheckResponse.named("database").withData("shares", count).up().build();
    } catch (Exception e) {
      return HealthCheckResponse.named("database").withData("error", e.getMessage()).down().build();
    }
  }
}
