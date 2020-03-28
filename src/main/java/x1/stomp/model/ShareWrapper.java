package x1.stomp.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@XmlRootElement(name = "shares")
@Schema(name = "shares", readOnly = true)
public abstract class ShareWrapper {
  private List<Share> shares;

  @XmlElement(name = "share")
  public List<Share> getShares() {
    return shares;
  }

  public void setShares(List<Share> shares) {
    this.shares = shares;
  }

  @Override
  public String toString() {
    return "Shares[quotes=" + shares + "]";
  }
}
