package x1.stomp.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonValue;

@XmlRootElement(name = "quotes")
public class Quotes {
  private List<Quote> quotes;

  public Quotes() {
  }

  public Quotes(List<Quote> quotes) {
    this.quotes = quotes;
  }

  @XmlElement(name = "quote")
  @JsonValue
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
