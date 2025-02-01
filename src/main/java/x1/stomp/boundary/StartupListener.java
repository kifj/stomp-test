package x1.stomp.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import org.slf4j.Logger;
import x1.stomp.control.ShareSubscription;

@ApplicationScoped
public class StartupListener {
    @Inject
    private Logger log;

    @Inject
    private ShareSubscription subscription;

    public void init(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
        var result = subscription.list();
        log.info("Initialized application {} with {} shares", context.getContextPath(), result.size());
    }

    public void destroy(@Observes @Destroyed(ApplicationScoped.class) ServletContext context) {
        log.info("Stopped application {}", context.getContextPath());
    }
}
