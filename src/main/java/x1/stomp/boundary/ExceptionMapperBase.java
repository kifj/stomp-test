package x1.stomp.boundary;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import static x1.stomp.boundary.MDCFilter.HTTP_STATUS_CODE;

/**
 * Base class for ExceptionMapper with MDC logging
 */
public abstract class ExceptionMapperBase {
  private final Logger log = LoggerFactory.getLogger(ExceptionMapperBase.class);

  @Context
  protected UriInfo uriInfo;

  protected void debug(Status status, String message, Object... args) {
    try {
      MDC.put(HTTP_STATUS_CODE, Integer.toString(status.getStatusCode()));
      log.debug(message, args);
    } finally {
      MDC.remove(HTTP_STATUS_CODE);
    }
  }

  protected void info(Status status, String message, Object... args) {
    try {
      MDC.put(HTTP_STATUS_CODE, Integer.toString(status.getStatusCode()));
      log.info(message, args);
    } finally {
      MDC.remove(HTTP_STATUS_CODE);
    }
  }

  protected void warn(Status status, String message, Object... args) {
    try {
      MDC.put(HTTP_STATUS_CODE, Integer.toString(status.getStatusCode()));
      log.warn(message, args);
    } finally {
      MDC.remove(HTTP_STATUS_CODE);
    }
  }

  protected void error(Status status, String message, Throwable t) {
    try {
      MDC.put(HTTP_STATUS_CODE, Integer.toString(status.getStatusCode()));
      log.error(message, t);
    } finally {
      MDC.remove(HTTP_STATUS_CODE);
    }
  }

}
