package x1.stomp.control;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@XmlRootElement(name = "QuickQuoteResponse")
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuickQuoteResponse implements Serializable {
  private static final long serialVersionUID = -7214747561822054360L;
  private QuickQuoteResult quickQuoteResult;

  @XmlElement(name = "QuickQuoteResult")
  public QuickQuoteResult getQuickQuoteResult() {
    return quickQuoteResult;
  }

  public void setQuickQuoteResult(QuickQuoteResult quickQuoteResult) {
    this.quickQuoteResult = quickQuoteResult;
  }

  @Override
  public String toString() {
    return "QuickQuoteResponse [quickQuoteResult=" + quickQuoteResult + "]";
  }
}
