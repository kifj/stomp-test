package x1.stomp.boundary;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
@Schema(description = "Structured error message")
public class ErrorMessage {
  public ErrorMessage() {
  }

  public ErrorMessage(String message) {
    this.message = message;
  }

  public ErrorMessage(String message, String path, Object invalidValue) {
    this.message = message;
    this.path = path;
    this.invalidValue = (invalidValue == null) ? null : invalidValue.toString();
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
