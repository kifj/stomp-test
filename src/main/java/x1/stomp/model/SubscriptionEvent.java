package x1.stomp.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SubscriptionEvent {
	private String action;
	private String key;

	public SubscriptionEvent() {
	}

	public SubscriptionEvent(String key, String action) {
		super();
		this.key = key;
		this.action = action;
	}

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
		return "<Event [action=" + action + ", key=" + key + "]>";
	}
}
