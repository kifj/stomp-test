package x1.stomp.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Executable action")
public enum Action {
  SUBSCRIBE,
  UNSUBSCRIBE
}
