package x1.stomp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonRootName;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "command")
@JsonRootName(value = "command")
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class Command {
  @NotNull
  private Action action;
  @NotNull
  @Schema(description = "the share")
  private String key;

  public Command() {
  }

  public Command(Action action, String key) {
    this.action = action;
    this.key = key;
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
    return "Command[action=" + action + ", key=" + key + "]";
  }
}
