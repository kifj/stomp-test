package x1.stomp.model;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonValue;

@XmlRootElement(name = "quotes")
public class Quotes extends QuoteWrapper {
  public Quotes() {
  }

  public Quotes(List<Quote> quotes) {
    setQuotes(quotes);
  }

  public static Quotes from(List<Quote> quotes) {
    return new Quotes(quotes);
  }

  @JsonValue
  public List<Quote> asJson() {
    return getQuotes();
  }
}
