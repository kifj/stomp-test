package x1.stomp.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.v3.oas.annotations.media.Schema;

@XmlRootElement(name = "quotes")
@Schema(name = "quotes")
public abstract class QuoteWrapper {
  private List<Quote> quotes;

  @XmlElement(name = "quote")
  public List<Quote> getQuotes() {
    return quotes;
  }

  public void setQuotes(List<Quote> quotes) {
    this.quotes = quotes;
  }

  @Override
  public String toString() {
    return "Quotes[quotes=" + quotes + "]";
  }
}
