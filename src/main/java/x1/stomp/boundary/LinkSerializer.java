package x1.stomp.boundary;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import javax.ws.rs.core.Link;

public class LinkSerializer extends JsonSerializer<Link> implements LinkConstants {

  @Override
  public void serialize(Link link, JsonGenerator jg, SerializerProvider sp) throws IOException {
    jg.writeStartObject();
    jg.writeStringField(PARAM_REL, link.getRel());
    jg.writeStringField(PARAM_HREF, link.getUri().toString());
    if (link.getType() != null) {
      jg.writeStringField(PARAM_TYPE, link.getType());
    }
    if (link.getTitle() != null) {
      jg.writeStringField(PARAM_TITLE, link.getTitle());
    }
    if (link.getParams().get(PARAM_METHOD) != null) {
      jg.writeStringField(PARAM_METHOD, link.getParams().get(PARAM_METHOD));
    }
    jg.writeEndObject();
  }
}
