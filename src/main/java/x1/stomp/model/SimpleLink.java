package x1.stomp.model;

import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "link", description = "HTTP Link")
public class SimpleLink {
  @Schema(description = "the link target as a URI-Reference", example = "https://microsoft.com", nullable = false)
  private String href;
  @Schema(description = "The relation type of a link", example = "self")
  private String rel;
  @Schema(description = "used to label the destination of a link such that it can be used as a human-readable identifier")
  private String title;
  @Schema(description = "a hint indicating what the media type of the result of dereferencing the link should be", 
      example = MediaType.TEXT_HTML)
  private String type;
  @Schema(description = "HTTP method", example = "get")
  private String method;

  public String getHref() {
    return href;
  }

  public void setHref(String href) {
    this.href = href;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getRel() {
    return rel;
  }

  public void setRel(String rel) {
    this.rel = rel;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

}
