package x1.stomp.test;

import java.util.UUID;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;

@Provider
public class RequestIdFilter implements ClientRequestFilter, ClientResponseFilter {
  public static final String X_REQUEST_ID = "X-Request-ID";

  @Inject
  private Logger log;

  @Override
  public void filter(ClientRequestContext requestContext) {
    var requestId = UUID.randomUUID().toString();
    requestContext.getHeaders().putSingle(X_REQUEST_ID, requestId);
    log.info("Set {} = {} for {}", X_REQUEST_ID, requestId, requestContext.getUri());
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
    String requestId = responseContext.getHeaders().getFirst(X_REQUEST_ID);
    log.info("Found {} = {} for {}", X_REQUEST_ID, requestId, requestContext.getUri());
  }
}
