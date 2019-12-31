package x1.stomp.util;

import java.util.Arrays;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.WebApplicationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import x1.stomp.boundary.MDCFilter;

@Interceptor
@Logged
public class LoggingInterceptor {

  @AroundInvoke
  public Object log(InvocationContext ctx) throws Exception {
    try {
      Logged annotation = ctx.getMethod().getAnnotation(Logged.class);
      if (annotation == null) {
        annotation = ctx.getMethod().getDeclaringClass().getAnnotation(Logged.class);
      }
      Object result = ctx.proceed();
      if (annotation != null && !annotation.onlyFailures()) {
        logCall(ctx, result);
      }
      return result;
    } catch (WebApplicationException e) {
      logFailure(ctx, e);
      throw e;
    } catch (Exception e) {
      logFailure(ctx, e);
      throw e;
    }
  }

  private void logCall(InvocationContext ctx, Object result) {
    var argLine = ctx.getMethod() + ": " + Arrays.toString(ctx.getParameters()) + " -> " + result;
    getLogger(ctx).debug(argLine);
  }

  private void logFailure(InvocationContext ctx, Exception e) {
    var argLine = ctx.getMethod() + ": " + Arrays.toString(ctx.getParameters()) + " failed";
    getLogger(ctx).error(argLine, e);
  }

  private void logFailure(InvocationContext ctx, WebApplicationException e) {
    var response = e.getResponse();
    try {
      MDC.put(MDCFilter.HTTP_STATUS_CODE, Integer.toString(response.getStatus()));
      var argLine = ctx.getMethod() + ": " + Arrays.toString(ctx.getParameters()) + " -> status="
          + response.getStatus();
      var log = getLogger(ctx);
      switch (response.getStatusInfo().getFamily()) {
      case INFORMATIONAL:
      case SUCCESSFUL:
      case REDIRECTION:
        log.debug(argLine);
        break;
      case CLIENT_ERROR:
      case OTHER:
        log.warn(argLine);
        break;
      case SERVER_ERROR:
        log.error(argLine, e);
        break;
      }
    } finally {
      MDC.remove(MDCFilter.HTTP_STATUS_CODE);
    }
  }

  private Logger getLogger(InvocationContext ctx) {
    return LoggerFactory.getLogger(ctx.getMethod().getDeclaringClass().getName());
  }
}
