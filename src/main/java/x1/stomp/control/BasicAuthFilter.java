package x1.stomp.control;

import java.io.IOException;

import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.internal.ClientRequestContextImpl;

public class BasicAuthFilter implements ClientRequestFilter {
  private BasicAuthentication delegate;
  private Boolean hasDelegate = null;

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    synchronized (this) {
      checkDelegate(requestContext);
    }
    if (hasDelegate == Boolean.TRUE) {
      delegate.filter(requestContext);
    }
  }

  private void checkDelegate(ClientRequestContext requestContext) {
    if (hasDelegate == null) {
      var config = CDI.current().select(Config.class).get();
      var clazz = getDeclaringClass(requestContext);
      var annotation = clazz.getAnnotation(RegisterRestClient.class);
      var configKey = annotation != null ? StringUtils.defaultIfEmpty(annotation.configKey(), clazz.getName())
          : clazz.getName();
      var username = config.getOptionalValue(configKey + "/mp-rest/username", String.class);
      var password = config.getOptionalValue(configKey + "/mp-rest/password", String.class);
      if (username.isPresent() && password.isPresent()) {
        delegate = new BasicAuthentication(username.get(), password.get());
        hasDelegate = Boolean.TRUE;
      } else {
        hasDelegate = Boolean.FALSE;
      }
    }
  }

  private Class<?> getDeclaringClass(ClientRequestContext requestContext) {
    if (requestContext instanceof ClientRequestContextImpl == false) {
      throw new RuntimeException(
          "Failed to get ClientInvocation from request context. Is RestEasy client used underneath?");
    }
    var invocation = ((ClientRequestContextImpl) requestContext).getInvocation();
    return invocation.getClientInvoker().getDeclaring();
  }
}