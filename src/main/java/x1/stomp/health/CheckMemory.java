package x1.stomp.health;

import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;

import javax.enterprise.context.ApplicationScoped;
import java.lang.management.ManagementFactory;

@ApplicationScoped
@Readiness
public class CheckMemory implements HealthCheck {
  @Override
  public HealthCheckResponse call() {

    var memoryBean = ManagementFactory.getMemoryMXBean();
    var memUsed = memoryBean.getHeapMemoryUsage().getUsed();
    var memMax = memoryBean.getHeapMemoryUsage().getMax();
    HealthCheckResponseBuilder builder = HealthCheckResponse.named("heap-memory").withData("used", memUsed)
        .withData("max", memMax);
    // status is is down is used memory is greater than 90% of max memory.
    builder = (memUsed < memMax * 0.9) ? builder.up() : builder.down();
    return builder.build();
  }
}
