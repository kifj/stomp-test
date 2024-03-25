package x1.stomp.control;

import java.io.Serial;
import java.io.Serializable;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@XmlRootElement(name = "QuickQuoteResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickQuoteResponse implements Serializable {
  @Serial
  private static final long serialVersionUID = -7214747561822054360L;
  private QuickQuoteResult quickQuoteResult;

  @XmlElement(name = "QuickQuoteResult")
  @JsonProperty(value = "QuickQuoteResult")
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
