package x1.stomp.boundary;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import javax.ws.rs.core.Link;

public class LinkSerializer extends JsonSerializer<Link> {

  @Override
  public void serialize(Link link, JsonGenerator jg, SerializerProvider sp) throws IOException {
    jg.writeStartObject();
    jg.writeStringField("rel", link.getRel());
    jg.writeStringField("href", link.getUri().toString());
    if (link.getType() != null) {
      jg.writeStringField("type", link.getType());
    }
    if (link.getTitle() != null) {
      jg.writeStringField("title", link.getTitle());
    }
    if (link.getParams().get("method") != null) {
      jg.writeStringField("method", link.getParams().get("method"));
    }
    jg.writeEndObject();
  }
}
