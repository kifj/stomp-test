package x1.stomp.util;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonHelper {
  private JsonHelper() {
  }

  public static String toJSON(Object obj) throws IOException {
    if (obj == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).enable(SerializationFeature.WRAP_ROOT_VALUE)
        .setSerializationInclusion(Include.NON_NULL);
    StringWriter sw = new StringWriter();
    mapper.writeValue(sw, obj);
    return sw.toString();
  }

  public static <T> T fromJSON(String content, Class<? extends T> resultClass) throws IOException {
    if (StringUtils.isEmpty(content)) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY).enable(DeserializationFeature.UNWRAP_ROOT_VALUE)
        .enable(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS);
    return mapper.readValue(content, resultClass);
  }

}
