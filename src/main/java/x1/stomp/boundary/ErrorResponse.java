package x1.stomp.boundary;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "errors")
@Schema(name = "errors", description = "Error response")
public class ErrorResponse {
  public ErrorResponse() {
    errors = new ArrayList<>();
  }
  
  public ErrorResponse(String type) {
    errors = new ArrayList<>();
    this.type = type;
  }

  @XmlElementRef(name = "error")
  @JsonProperty(value = "error")
  public List<ErrorMessage> getErrors() {
    return errors;
  }

  public void setErrors(List<ErrorMessage> errors) {
    this.errors = errors;
  }

  public void add(ErrorMessage errorMessage) {
    errors.add(errorMessage);
  }
  
  @XmlAttribute
  @Schema(description = "Error type")
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  @XmlAttribute
  @Schema(description = "Request URI")
  public String getRequestUri() {
    return requestUri;
  }
  
  public void setRequestUri(String requestUri) {
    this.requestUri = requestUri;
  }

  @Override
  public String toString() {
    return "ErrorResponse[requestUri=" + requestUri + ", type=" + type + ", errors=" + errors + "]";
  }

  private List<ErrorMessage> errors;
  private String requestUri;
  private String type;
}
