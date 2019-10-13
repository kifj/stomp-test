package x1.stomp.test;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;

import x1.stomp.boundary.ErrorResponse;

public class ErrorResponseAssert extends AbstractAssert<ErrorResponseAssert, ErrorResponse> {

  private ErrorResponseAssert(ErrorResponse actual) {
    super(actual, ErrorResponseAssert.class);
  }

  public static ErrorResponseAssert assertThat(ErrorResponse actual) {
    return new ErrorResponseAssert(actual);
  }

  public ErrorResponseAssert hasRequestUri() {
    Assertions.assertThat(actual.getRequestUri()).isNotNull();
    return this;
  }
  
  public ErrorResponseAssert hasType(String type) {
    Assertions.assertThat(actual.getType()).isEqualTo(type);
    return this;
  }
  
  public ErrorResponseAssert containsErrors(int size) {
    Assertions.assertThat(actual.getErrors()).hasSize(size);
    return this;
  }

}