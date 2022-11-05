package x1.stomp.boundary;

import java.util.UUID;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

@Provider
@PreMatching
public class MDCFilter implements ContainerRequestFilter, ContainerResponseFilter {

  public static final String X_REQUEST_ID = "X-Request-ID";
  public static final String X_CALLER_ID = "X-Caller-ID";
  public static final String REQUEST_ID = "requestId";
  public static final String CALLER_ID = "callerId";
  public static final String HTTP_STATUS_CODE = "http_response_code";

  @Override
  public void filter(ContainerRequestContext requestContext) {
    var requestId = requestContext.getHeaderString(X_REQUEST_ID);
    if (StringUtils.isEmpty(requestId)) {
      requestId = UUID.randomUUID().toString();
    }
    MDC.put(REQUEST_ID, requestId);
    var callerId = requestContext.getHeaderString(X_CALLER_ID);
    if (StringUtils.isNotEmpty(callerId)) {
      MDC.put(CALLER_ID, callerId);
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    responseContext.getHeaders().putSingle(X_REQUEST_ID, MDC.get(REQUEST_ID));
    MDC.remove(REQUEST_ID);
    MDC.remove(CALLER_ID);
  }
}
