package x1.stomp.util;

import org.apache.commons.lang3.StringUtils;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Timer.Sample;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

/**
 * Wildfly has currently no annotation binding for micrometer annotations
 */
@Interceptor
@Metered
public class MeteredInterceptor {

  @Inject
  private MeterRegistry registry;

  @AroundInvoke
  public Object meter(InvocationContext ctx) throws Exception {
    Sample sample = null;
    Timer timer = null;
    Counter counter = null;
    try {
      var counted = counted(ctx);
      if (counted != null) {
        counter = registry.counter(metricId(counted, ctx.getMethod().getDeclaringClass(), ctx.getMethod().getName()),
            counted.extraTags());
      }
      var timed = timed(ctx);
      if (timed != null) {
        timer = registry.timer(metricId(timed, ctx.getMethod().getDeclaringClass(), ctx.getMethod().getName()),
            timed.extraTags());
        sample = Timer.start(registry);
      }
      return ctx.proceed();
    } finally {
      if (sample != null) {
        sample.stop(timer);
      }
      if (counter != null) {
        counter.increment();
      }
    }
  }

  private Timed timed(InvocationContext ctx) {
    var annotation = ctx.getMethod().getAnnotation(Timed.class);
    if (annotation == null) {
      annotation = ctx.getMethod().getDeclaringClass().getAnnotation(Timed.class);
    }
    return annotation;
  }

  private Counted counted(InvocationContext ctx) {
    var annotation = ctx.getMethod().getAnnotation(Counted.class);
    if (annotation == null) {
      annotation = ctx.getMethod().getDeclaringClass().getAnnotation(Counted.class);
    }
    return annotation;
  }

  private String metricId(Timed annotation, Class<?> type, String methodName) {
    return StringUtils.defaultIfEmpty(annotation.value(), type.getSimpleName() + "." + methodName);
  }

  private String metricId(Counted annotation, Class<?> type, String methodName) {
    return StringUtils.defaultIfEmpty(annotation.value(), type.getSimpleName() + "." + methodName);
  }
}
