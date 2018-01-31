package x1.stomp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Command {
  public static final String ACTION_SUBSCRIBE = "SUBSCRIBE";
  public static final String ACTION_UNSUBSCRIBE = "UNSUBSCRIBE";

  private String action;
  private String key;

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  @Override
  public String toString() {
    return "<Command [action=" + action + ", key=" + key + "]>";
  }
}
