package x1.stomp.boundary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.Link;
import java.io.IOException;

public class LinkDeserializer extends JsonDeserializer<Link> implements LinkConstants {

  @Override
  public Link deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    ObjectCodec oc = jp.getCodec();
    JsonNode node = oc.readTree(jp);
    String href = node.get(PARAM_HREF) != null ? node.get(PARAM_HREF).asText() : "";
    String rel = node.get(PARAM_REL) != null ? node.get(PARAM_REL).asText() : null;
    String title = node.get(PARAM_TITLE) != null ? node.get(PARAM_TITLE).asText() : null;
    Link.Builder builder = Link.fromUri(href);
    if (rel != null) {
      builder = builder.rel(rel);
    }
    if (title != null) {
      builder = builder.rel(title);
    }
    return builder.build();
  }
}
