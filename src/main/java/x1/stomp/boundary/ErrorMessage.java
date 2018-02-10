package x1.stomp.boundary;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "error")
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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getInvalidValue() {
    return invalidValue;
  }

  public void setInvalidValue(String invalidValue) {
    this.invalidValue = invalidValue;
  }

  @Override
  public String toString() {
    return "<ErrorMessage [message=" + message + ", path=" + path + ", invalidValue=" + invalidValue + "]>";
  }

  private String message;
  private String path;
  private String invalidValue;
}
