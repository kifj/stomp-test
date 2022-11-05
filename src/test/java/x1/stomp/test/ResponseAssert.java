package x1.stomp.test;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.assertj.core.api.AbstractAssert;

public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

  private ResponseAssert(Response actual) {
    super(actual, ResponseAssert.class);
  }

  public static ResponseAssert assertThat(Response actual) {
    return new ResponseAssert(actual);
  }

  @SuppressWarnings("UnusedReturnValue")
  public ResponseAssert hasStatus(Status status) {
    isNotNull();
    if (actual.getStatus() != status.getStatusCode()) {
      failWithMessage("Expected status code to be <%s> but was <%s>", status.getStatusCode(), actual.getStatus());
    }
    return this;
  }
}