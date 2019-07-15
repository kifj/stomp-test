package x1.stomp.boundary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import javax.ws.rs.core.Link;
import java.io.IOException;

public class LinkDeserializer extends JsonDeserializer<Link> {

  @Override
  public Link deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
    ObjectCodec oc = jp.getCodec();
    JsonNode node = oc.readTree(jp);
    String href = node.get("href") != null ? node.get("href").asText() : "";
    String rel = node.get("rel") != null ? node.get("rel").asText() : null;
    String title = node.get("title") != null ? node.get("title").asText() : null;
    Link.Builder builder = Link.fromUri(href);
    if (rel !=null ) {
      builder = builder.rel(rel);
    }
    if (title !=null ) {
      builder = builder.rel(title);
    }
    return builder.build();
  }
}
