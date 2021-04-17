package x1.stomp.boundary;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.annotation.RegistryType;

@Provider
public class ResponseStatusMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {
  private static final String METRIC_ID_PARAM = "x1.stomp.boundary.metricID";

  @Context
  private ResourceInfo resourceInfo;

  @Inject
  @RegistryType(type = MetricRegistry.Type.APPLICATION)
  private MetricRegistry registry;

  @Override
  public void filter(ContainerRequestContext requestContext) {
    getMetricID(resourceInfo.getResourceClass(), resourceInfo.getResourceMethod())
        .ifPresent(metricID -> requestContext.setProperty(METRIC_ID_PARAM, metricID));
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    var metricID = (MetricID) requestContext.getProperty(METRIC_ID_PARAM);
    if (metricID != null) {
      createMetrics(metricID, responseContext.getStatus());
    }
  }

  private void createMetrics(MetricID metricID, int status) {
    var statusTag = new Tag("status", String.valueOf(status));
    var metadata = Metadata.builder().withName(metricID.getName()).build();
    var tags = new ArrayList<>(metricID.getTagsAsList());
    tags.add(statusTag);
    registry.counter(metadata, tags.toArray(new Tag[0])).inc();
  }

  private Optional<MetricID> getMetricID(Class<?> resourceClass, Method resourceMethod) {
    var counted = resourceMethod.getAnnotation(RestRequestStatusCounted.class);
    if (counted == null) {
      counted = resourceClass.getAnnotation(RestRequestStatusCounted.class);
    }
    if (counted == null) {
      return Optional.empty();
    }
    var classTag = new Tag("class", resourceClass.getSimpleName());
    var methodTag = new Tag("method", resourceMethod.getName());
    return Optional.of(new MetricID("rest-request-status", classTag, methodTag));
  }
}
