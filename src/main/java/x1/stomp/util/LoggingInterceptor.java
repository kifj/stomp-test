package x1.stomp.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import static x1.stomp.boundary.MDCFilter.HTTP_STATUS_CODE;

@Interceptor
@Logged
public class LoggingInterceptor {

  @AroundInvoke
  public Object log(InvocationContext ctx) throws Exception {
    var keys = new ArrayList<String>();
    try {
      var annotation = ctx.getMethod().getAnnotation(Logged.class);
      if (annotation == null) {
        annotation = ctx.getMethod().getDeclaringClass().getAnnotation(Logged.class);
      }
      if (annotation != null) {
        keys = withMdc(ctx);
      }
      var result = ctx.proceed();
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
    } finally {
      clearMdc(keys);
    }
  }

  private void logCall(InvocationContext ctx, Object result) {
    var argLine = ctx.getMethod().getName() + "(" + StringUtils.join(ctx.getParameters(), ',') + ") -> " + result;
    getLogger(ctx).debug(argLine);
  }

  private void logFailure(InvocationContext ctx, Exception e) {
    var argLine = ctx.getMethod().getName() + "(" + StringUtils.join(ctx.getParameters(), ',') + ") failed";
    getLogger(ctx).error(argLine, e);
  }

  private void logFailure(InvocationContext ctx, WebApplicationException e) {
    try {
      MDC.put(HTTP_STATUS_CODE, Integer.toString(e.getResponse().getStatus()));
      logResponse(ctx, e);
    } finally {
      MDC.remove(HTTP_STATUS_CODE);
    }
  }

  private void logResponse(InvocationContext ctx, WebApplicationException e) {
    var response = e.getResponse();
    var argLine = ctx.getMethod().getName() + "(" + StringUtils.join(ctx.getParameters(), ',') + ") -> status="
        + response.getStatus();
    var log = getLogger(ctx);
    switch (response.getStatusInfo().getFamily()) {
    case INFORMATIONAL, SUCCESSFUL, REDIRECTION -> log.debug(argLine);
    case CLIENT_ERROR, OTHER -> log.warn(argLine);
    case SERVER_ERROR -> log.error(argLine, e);
    }
  }

  private Logger getLogger(InvocationContext ctx) {
    return LoggerFactory.getLogger(ctx.getMethod().getDeclaringClass().getName());
  }

  private ArrayList<String> withMdc(InvocationContext ctx) {
    var keys = new ArrayList<String>();
    var method = ctx.getMethod();
    var parameters = method.getParameters();

    var i = 0;
    for (var annotations : method.getParameterAnnotations()) {
      for (var annotation : annotations) {
        if (annotation instanceof MDCKey mdcKey) {
          var key = StringUtils.defaultIfEmpty(mdcKey.value(), parameters[i].getName());
          var value = Objects.toString(ctx.getParameters()[i], null);
          if (value != null) {
            keys.add(key);
            MDC.put(key, value);
          }
        }
      }
      i++;
    }
    return keys;
  }

  private void clearMdc(List<String> keys) {
    keys.forEach(MDC::remove);
  }

}
