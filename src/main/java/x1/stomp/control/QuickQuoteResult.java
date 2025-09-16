package x1.stomp.control;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickQuoteResult implements Serializable {
  @Serial
  private static final long serialVersionUID = -7297678762119016793L;

  @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
  @JsonProperty(value = "QuickQuote")
  private List<QuickQuote> quotes = new ArrayList<>();

  public List<QuickQuote> getQuotes() {
    return quotes;
  }

  public void setQuotes(List<QuickQuote> quotes) {
    this.quotes = quotes;
  }

  @Override
  public String toString() {
    return "QuickQuoteResult[quotes=" + quotes + "]";
  }
}
