package x1.stomp.boundary;

import java.util.Arrays;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.slf4j.Logger;

@Interceptor
@Logged
public class LoggingInterceptor {
  @Inject
  private Logger log;

  @AroundInvoke
  public Object logExceptions(InvocationContext ctx) throws Exception {
    try {
      return ctx.proceed();
    } catch (RuntimeException e) {      
      String argLine = ctx.getMethod() + ": " + Arrays.toString(ctx.getParameters());
      log.error("Error at " + argLine, e);
      throw e;
    }
  }
}
