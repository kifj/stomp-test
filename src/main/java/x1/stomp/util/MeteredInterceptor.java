package x1.stomp.util;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.micrometer.core.instrument.*;
import org.apache.commons.lang3.StringUtils;

import io.micrometer.core.annotation.Counted;
import io.micrometer.core.annotation.Timed;
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
    private static final Map<String, Optional<? extends Annotation>> CACHE = new ConcurrentHashMap<>();

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
                sample = Timer.start();
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
            registry.counter(metricId(counted), Tags.of(Tag.of("class", type.getSimpleName()), Tag.of("method", method),
                    Tag.of(EXCEPTION_TAG, getExceptionTag(exception))).and(counted.extraTags()))
                    .increment();
        }
    }

    private void stopTimer(Class<?> type, String method, Timed timed, Timer.Sample sample, Exception exception) {
        var timer = Timer.builder(metricId(timed))
                .tags(Tags.of(Tag.of("class", type.getSimpleName()), Tag.of("method", method),
                        Tag.of(EXCEPTION_TAG, getExceptionTag(exception))).and(timed.extraTags()))
                // WFLY-21339: this is currently not working
                .publishPercentileHistogram(timed.histogram())
                .publishPercentiles(timed.percentiles())
                .register(registry);
        sample.stop(timer);
    }

    private Timed timed(InvocationContext ctx) {
        var cached = CACHE.get(signature("timed", ctx));
        if (cached == null) {
            var annotation = ctx.getMethod().getAnnotation(Timed.class);
            if (annotation == null) {
                annotation = ctx.getMethod().getDeclaringClass().getAnnotation(Timed.class);
            }
            CACHE.put(signature("timed", ctx), Optional.ofNullable(annotation));
            if (annotation != null && annotation.longTask()) {
                throw new UnsupportedOperationException("longTask is not supported by this interceptor");
            }
            return annotation;
        } else {
            return (Timed) cached.orElse(null);
        }
    }

    private Counted counted(InvocationContext ctx) {
        var cached = CACHE.get(signature("counted", ctx));
        if (cached == null) {
            var annotation = ctx.getMethod().getAnnotation(Counted.class);
            if (annotation == null) {
                annotation = ctx.getMethod().getDeclaringClass().getAnnotation(Counted.class);
            }
            CACHE.put(signature("counted", ctx), Optional.ofNullable(annotation));
            return annotation;
        } else {
            return (Counted) cached.orElse(null);
        }
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

  private String signature(String prefix, InvocationContext ctx) {
    return prefix + "/" + ctx.getMethod().getDeclaringClass().getName() + "." + ctx.getMethod().getName();
  }

}
