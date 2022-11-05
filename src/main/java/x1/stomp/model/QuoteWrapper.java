package x1.stomp.model;

import java.util.List;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@XmlRootElement(name = "quotes")
@Schema(name = "quotes", readOnly = true)
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
