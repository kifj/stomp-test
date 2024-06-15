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
  public static final String DEFAULT_METRIC_NAME = "method.timed";
  public static final String DEFAULT_EXCEPTION_TAG_VALUE = "none";
  public static final String EXCEPTION_TAG = "exception";

  @Inject
  private MeterRegistry registry;

  @AroundInvoke
  public Object meter(InvocationContext ctx) throws Exception {
    var type = ctx.getMethod().getDeclaringClass();
    var method = ctx.getMethod().getName();

    var timed = timed(ctx);
    var counted = counted(ctx);
    
    Sample sample = null;
    Exception exception = null;
    try {
      if (timed != null) {
        sample = Timer.start(registry);
      }
      return ctx.proceed();
    } catch (Exception e) {
      exception = e;
      throw e;
    } finally {
      if (sample != null) {
        stopTimer(type, method, timed, sample, exception);
      }
      if (counted != null) {
        increaseCounter(type, method, counted, exception);
      }
    }
  }

  private void increaseCounter(Class<?> type, String method, Counted counted, Exception exception) {
    if (!counted.recordFailuresOnly() || exception != null) {
      Counter.builder(metricId(counted)).description(StringUtils.defaultIfEmpty(counted.description(), null))
          .tags(counted.extraTags()).tag("class", type.getSimpleName()).tag("method", method)
          .tags(EXCEPTION_TAG, getExceptionTag(exception)).register(registry).increment();
    }
  }

  private void stopTimer(Class<?> type, String method, Timed timed, Sample sample, Exception exception) {
    var timer = Timer.builder(metricId(timed)).description(StringUtils.defaultIfEmpty(timed.description(), null))
        .tags(timed.extraTags()).tag("class", type.getSimpleName()).tag("method", method)
        .tags(EXCEPTION_TAG, getExceptionTag(exception)).register(registry);
    sample.stop(timer);
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

  private String metricId(Timed annotation) {
    return StringUtils.defaultIfEmpty(annotation.value(), DEFAULT_METRIC_NAME);
  }

  private String metricId(Counted annotation) {
    return annotation.value();
  }

  private String getExceptionTag(Throwable throwable) {
    if (throwable == null) {
      return DEFAULT_EXCEPTION_TAG_VALUE;
    }
    if (throwable.getCause() == null) {
      return throwable.getClass().getSimpleName();
    }
    return throwable.getCause().getClass().getSimpleName();
  }
}
