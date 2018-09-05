package x1.stomp.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;

import io.swagger.v3.oas.annotations.media.Schema;

@XmlRootElement(name = "quote")
@JsonRootName(value = "quote")
@Schema(description = "A quote is the current price for a share")
public class Quote implements Serializable {
  private static final long serialVersionUID = -6139640371442481033L;

  @Schema(description = "the share", required = true)
  private Share share;
  @Schema(description = "the price")
  private Float price;
  @Schema(description = "currency code")
  private String currency;
  @Schema(description = "date of origin, as ISO8601")
  private Date from;

  public Quote() {
  }

  public Quote(Share share) {
    this.share = share;
  }

  public Share getShare() {
    return share;
  }

  public void setShare(Share share) {
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

  @Override
  public String toString() {
    return "Quote[share=" + share + ", price=" + price + ", currency=" + currency + ", from=" + from + "]";
  }

}
