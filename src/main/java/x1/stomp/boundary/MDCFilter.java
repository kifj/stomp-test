package x1.stomp.boundary;

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

@Provider
@PreMatching
public class MDCFilter implements ContainerRequestFilter, ContainerResponseFilter {
  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    String requestId = requestContext.getHeaderString("X-Request-ID");
    if (StringUtils.isEmpty(requestId)) {
      requestId = UUID.randomUUID().toString();
    }
    MDC.put("requestId", requestId);
    String callerId = requestContext.getHeaderString("X-Caller-ID");
    if (StringUtils.isNotEmpty(callerId)) {
      MDC.put("callerId", callerId);
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
      throws IOException {
    responseContext.getHeaders().putSingle("X-Request-ID", MDC.get("requestId"));
    MDC.remove("requestId");
    MDC.remove("callerId");
  }
}
