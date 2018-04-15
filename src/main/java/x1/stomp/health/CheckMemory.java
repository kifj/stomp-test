package x1.stomp.health;

import org.eclipse.microprofile.health.Health;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import javax.enterprise.context.ApplicationScoped;
import java.lang.management.MemoryMXBean;
import java.lang.management.ManagementFactory;

@ApplicationScoped
@Health
public class CheckMemory implements HealthCheck {
    @Override
    public HealthCheckResponse call() {

        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long memUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long memMax = memoryBean.getHeapMemoryUsage().getMax();
        HealthCheckResponseBuilder builder = HealthCheckResponse.named("heap-memory")
                    .withData("used", memUsed)
                    .withData("max", memMax);
         // status is is down is used memory is greater than 90% of max memory.
         builder = (memUsed < memMax * 0.9) ? builder .up() : builder .down();
         return builder .build(); 
     }
}
