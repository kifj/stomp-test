package x1.stomp.test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.assertj.core.api.AbstractAssert;

public class ResponseAssert extends AbstractAssert<ResponseAssert, Response> {

  public ResponseAssert(Response actual) {
    super(actual, ResponseAssert.class);
  }

  public static ResponseAssert assertThat(Response actual) {
    return new ResponseAssert(actual);
  }

  public ResponseAssert hasStatus(Status status) {
    isNotNull();
    if (actual.getStatus() != status.getStatusCode()) {
      failWithMessage("Expected status code to be <%s> but was <%s>", status.getStatusCode(), actual.getStatus());
    }
    return this;
  }
}