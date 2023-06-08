package x1.stomp.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.core.Link;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.eclipse.microprofile.openapi.annotations.ExternalDocumentation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@SuppressWarnings("deprecation")
@XmlRootElement(name = "quote")
@JsonRootName(value = "quote")
@Schema(name = "quote", description = "A quote is the current price for a share", readOnly = true)
@XmlAccessorType(XmlAccessType.FIELD)
public class Quote implements Serializable {
  private static final long serialVersionUID = -6139640371442481033L;

  @Schema(description = "the share", required = true)
  @XmlElement
  private Share share;
  @Schema(description = "the price", example = "12.34")
  @XmlAttribute
  private Float price;
  @Schema(description = "currency code", defaultValue = "EUR")
  @XmlAttribute
  private String currency;
  @Schema(description = "date of origin, as defined in `ISO8601`", 
       externalDocs = @ExternalDocumentation(url = "https://en.wikipedia.org/wiki/ISO_8601"))
  @XmlAttribute
  private Date from;
  @JsonProperty(value = "links")
  @XmlElement(name = "link")
  @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
  @Schema(type=SchemaType.ARRAY, implementation = SimpleLink.class, readOnly = true)
  private List<Link> links;

  public Quote() {
  }

  public Quote(@NotNull Share share) {
    this.share = share;
  }

  public Share getShare() {
    return share;
  }

  public void setShare(@NotNull Share share) {
    this.share = share;
  }

  public Float getPrice() {
    return price;
  }

  public void setPrice(Float price) {
    this.price = price;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Date getFrom() {
    return from;
  }

  public void setFrom(Date from) {
    this.from = from;
  }

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  @Override
  public String toString() {
    return "Quote[share=" + share + ", price=" + price + ", currency=" + currency + ", from=" + from + "]";
  }

}
