package x1.stomp.health;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
@Health
public class DatabaseCheck implements HealthCheck {

  @Inject
  private EntityManager em;

  @Override
  public HealthCheckResponse call() {
    try {
      Long count = em.createQuery("SELECT COUNT(s.id) FROM Share s", Long.class).getSingleResult();
      return HealthCheckResponse.named("database").withData("shares", count).up().build();
    } catch (Exception e) {
      return HealthCheckResponse.named("database").withData("error", e.getMessage()).down().build();
    }
  }
}
