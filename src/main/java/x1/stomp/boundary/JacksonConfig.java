package x1.stomp.boundary;

import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.text.SimpleDateFormat;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Provider
@Produces(APPLICATION_JSON)
public class JacksonConfig implements ContextResolver<ObjectMapper> {
  public static final String DATE_FORMAT_STR_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ";

  private final ObjectMapper mapper;

  public JacksonConfig() {
    mapper = new ObjectMapper().disable(WRITE_DATES_AS_TIMESTAMPS).setSerializationInclusion(Include.NON_NULL)
        .enable(ACCEPT_SINGLE_VALUE_AS_ARRAY).enable(UNWRAP_SINGLE_VALUE_ARRAYS).disable(FAIL_ON_IGNORED_PROPERTIES)
        .disable(FAIL_ON_UNKNOWN_PROPERTIES).enable(INDENT_OUTPUT).disable(WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
        .disable(WRITE_ENUMS_USING_TO_STRING);
    mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT_STR_ISO8601));

    var simpleModule = new SimpleModule();
    simpleModule.addSerializer(Link.class, new LinkSerializer());
    simpleModule.addDeserializer(Link.class, new LinkDeserializer());
    mapper.registerModule(simpleModule);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return getObjectMapper();
  }
  
  private ObjectMapper getObjectMapper() {
    return mapper;
  }

}
