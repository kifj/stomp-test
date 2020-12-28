package x1.stomp.boundary;

import java.util.List;

import javax.ws.rs.core.Link;
import javax.ws.rs.core.Link.JaxbAdapter;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
  @XmlJavaTypeAdapter(JaxbAdapter.class)
  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }
}
