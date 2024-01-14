package x1.stomp.boundary;

import java.util.List;

import jakarta.ws.rs.core.Link;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import x1.stomp.model.JaxbSupport;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "index")
@Schema(name = "index", description = "Index page with HATEOS links")
public class IndexResponse {
  public IndexResponse() {
  }

  public IndexResponse(List<Link> links) {
    this.links = links;
  }

  private List<Link> links;

  @JsonProperty(value = "links")
  @XmlElement(name = "link")
  @XmlJavaTypeAdapter(JaxbSupport.JaxbAdapter.class)
  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }
}
