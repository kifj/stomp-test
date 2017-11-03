package x1.stomp.service;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "QuickQuoteResult")
public class QuickQuoteResult implements Serializable {
  private static final long serialVersionUID = -7297678762119016793L;

  private List<QuickQuote> quotes;

  @XmlElement(name = "QuickQuote")
  public List<QuickQuote> getQuotes() {
    return quotes;
  }

  public void setQuotes(List<QuickQuote> quotes) {
    this.quotes = quotes;
  }

  @Override
  public String toString() {
    return "QuickQuoteResult [quotes=" + quotes + "]";
  }
}
