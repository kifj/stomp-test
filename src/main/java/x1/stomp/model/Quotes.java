package x1.stomp.model;

import java.util.List;

import javax.xml.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonValue;

@XmlRootElement(name = "quotes")
@XmlAccessorType(XmlAccessType.FIELD)
public class Quotes {
  @XmlElement(name = "quote")
  private List<Quote> quotes;

  public Quotes() {
  }

  public Quotes(List<Quote> quotes) {
    this.quotes = quotes;
  }

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
