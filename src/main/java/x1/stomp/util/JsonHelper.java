package x1.stomp.util;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.fasterxml.jackson.databind.SerializationFeature.*;

public class JsonHelper {
  private final ObjectMapper mapper;

  public JsonHelper() {
    mapper = new ObjectMapper().enable(WRAP_ROOT_VALUE).enable(ACCEPT_SINGLE_VALUE_AS_ARRAY).enable(UNWRAP_ROOT_VALUE)
        .enable(UNWRAP_SINGLE_VALUE_ARRAYS).disable(WRITE_DATES_AS_TIMESTAMPS).disable(FAIL_ON_UNKNOWN_PROPERTIES)
        .setSerializationInclusion(Include.NON_NULL);
  }

  public String toJSON(Object obj) throws IOException {
    if (obj == null) {
      return null;
    }
    return mapper.writeValueAsString(obj);
  }

  public <T> T fromJSON(String content, Class<? extends T> resultClass) throws IOException {
    if (StringUtils.isEmpty(content)) {
      return null;
    }
    return mapper.readValue(content, resultClass);
  }

}
