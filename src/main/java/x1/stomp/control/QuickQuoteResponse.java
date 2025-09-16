package x1.stomp.control;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickQuoteResponse implements Serializable {
  @Serial
  private static final long serialVersionUID = -7214747561822054360L;
  @JsonProperty(value = "QuickQuoteResult")
  private QuickQuoteResult quickQuoteResult;

  public QuickQuoteResult getQuickQuoteResult() {
    return quickQuoteResult;
  }

  public void setQuickQuoteResult(QuickQuoteResult quickQuoteResult) {
    this.quickQuoteResult = quickQuoteResult;
  }

  @Override
  public String toString() {
    return "QuickQuoteResponse[quickQuoteResult=" + quickQuoteResult + "]";
  }
}
