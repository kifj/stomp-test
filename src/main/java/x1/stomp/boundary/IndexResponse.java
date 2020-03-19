package x1.stomp.boundary;

import java.util.List;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@XmlRootElement(name = "index")
@Schema(description = "Index page with HATEOS links")
public class IndexResponse {
  public IndexResponse() {
  }

  public IndexResponse(List<Link> links) {
    this.links = links;
  }

  private List<Link> links;

  @JsonProperty(value = "links")
  @XmlElement(name = "link")
  @XmlJavaTypeAdapter(JaxbAdapter.class)
  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }
}