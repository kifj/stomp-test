package x1.stomp.control;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "QuickQuoteResult")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickQuoteResult implements Serializable {
  private static final long serialVersionUID = -7297678762119016793L;

  private List<QuickQuote> quotes;

  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  @JsonProperty(value = "QuickQuote")
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
