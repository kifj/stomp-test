package x1.stomp.model;

import jakarta.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;

@XmlRootElement(name = "subscriptionEvent")
@JsonRootName(value = "subscriptionEvent")
public class SubscriptionEvent {
  private Action action;
  private String key;

  public SubscriptionEvent() {
  }

  public SubscriptionEvent(Action action, String key) {
    this.key = key;
    this.action = action;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
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
    return "SubscriptionEvent[action=" + action + ", key=" + key + "]";
  }
}
