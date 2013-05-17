package x1.stomp.rest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "response")
public class ErrorResponse {
  public ErrorResponse() {
    errors = new ArrayList<ErrorMessage>();
  }

  public ErrorResponse(List<ErrorMessage> errors) {
    this.errors = errors;
  }

  @XmlElementRef(name = "error")
  public List<ErrorMessage> getErrors() {
    return errors;
  }

  public void setErrors(List<ErrorMessage> errors) {
    this.errors = errors;
  }

  public void add(ErrorMessage errorMessage) {
    errors.add(errorMessage);
  }

  private List<ErrorMessage> errors;

}
