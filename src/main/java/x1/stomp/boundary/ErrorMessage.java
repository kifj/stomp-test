package x1.stomp.boundary;

import javax.validation.ConstraintViolation;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@XmlRootElement(name = "error")
@Schema(name = "error", description = "Structured error message")
public class ErrorMessage {
  public static ErrorMessage of(String message) {
    return new ErrorMessage(message);
  }

  public static ErrorMessage from(Exception e) {
    return new ErrorMessage(e.getMessage());
  }

  private ErrorMessage() {
  }

  private ErrorMessage(String message) {
    this.message = message;
  }

  public static ErrorMessage of(ConstraintViolation<?> violation) {
    ErrorMessage errorMessage = new ErrorMessage();
    errorMessage.message = violation.getMessage();
    errorMessage.path = violation.getPropertyPath().toString();
    errorMessage.invalidValue = (violation.getInvalidValue() == null) ? null : violation.getInvalidValue().toString();
    return errorMessage;
  }
  
  @XmlAttribute
  @Schema(description = "The error message")
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @XmlAttribute
  @Schema(description = "On validation errors: the property which was invalid")
  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @XmlAttribute
  @Schema(description = "On validation errors: the value which was invalid")
  public String getInvalidValue() {
    return invalidValue;
  }

  public void setInvalidValue(String invalidValue) {
    this.invalidValue = invalidValue;
  }

  @Override
  public String toString() {
    return "ErrorMessage[message=" + message + ", path=" + path + ", invalidValue=" + invalidValue + "]";
  }

  private String message;
  private String path;
  private String invalidValue;
}
