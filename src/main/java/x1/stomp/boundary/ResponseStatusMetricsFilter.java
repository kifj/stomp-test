package x1.stomp.boundary;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

@Provider
public class ResponseStatusMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {
  private static final String METRIC_ID_PARAM = "x1.stomp.boundary.metricID";

  @Context
  private ResourceInfo resourceInfo;

  @Inject
  private MeterRegistry registry;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    getMetricID(resourceInfo.getResourceClass(), resourceInfo.getResourceMethod())
        .ifPresent(metricID -> requestContext.setProperty(METRIC_ID_PARAM, metricID));
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    var metricID = (String) requestContext.getProperty(METRIC_ID_PARAM);
    if (metricID != null) {
      var classTag = Tag.of("class", resourceInfo.getResourceClass().getSimpleName());
      var methodTag = Tag.of("method", resourceInfo.getResourceMethod().getName());
      var statusTag = Tag.of("status", String.valueOf(responseContext.getStatus()));
      createMetrics(metricID, classTag, methodTag, statusTag);
      requestContext.removeProperty(METRIC_ID_PARAM);
    }
  }

  private void createMetrics(String metricID, Tag... tags) {
    registry.counter(metricID, Arrays.asList(tags)).increment();
  }

  private Optional<String> getMetricID(Class<?> resourceClass, Method resourceMethod) {
    var counted = resourceMethod.getAnnotation(RestRequestStatusCounted.class);
    if (counted == null) {
      counted = resourceClass.getAnnotation(RestRequestStatusCounted.class);
    }
    if (counted == null) {
      return Optional.empty();
    }
    return Optional.of("rest-request-status");
  }
}
