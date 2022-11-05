package x1.stomp.health;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import jakarta.enterprise.context.ApplicationScoped;
import java.lang.management.ManagementFactory;

@ApplicationScoped
@Liveness
public class CheckMemory implements HealthCheck {
  @Override
  public HealthCheckResponse call() {

    var memoryBean = ManagementFactory.getMemoryMXBean();
    var memUsed = memoryBean.getHeapMemoryUsage().getUsed();
    var memMax = memoryBean.getHeapMemoryUsage().getMax();
    var builder = HealthCheckResponse.named("heap-memory").withData("used", memUsed).withData("max", memMax);
    if (memMax > 0) {
      // status is is down is used memory is greater than 90% of max memory.
      builder = (memUsed < memMax * 0.9) ? builder.up() : builder.down();
    } else {
      builder = builder.up();
    }
    return builder.build();
  }
}
