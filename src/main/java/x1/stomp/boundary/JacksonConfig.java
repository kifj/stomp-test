package x1.stomp.boundary;

import java.text.SimpleDateFormat;

import javax.ws.rs.Produces;
import javax.ws.rs.core.Link;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import static com.fasterxml.jackson.databind.DeserializationFeature.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import static com.fasterxml.jackson.databind.SerializationFeature.*;

@Provider
@Produces(APPLICATION_JSON)
public class JacksonConfig implements ContextResolver<ObjectMapper> {
  public final static String DATE_FORMAT_STR_ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ";

  private final ObjectMapper mapper;

  public JacksonConfig() {
    mapper = new ObjectMapper().disable(WRITE_DATES_AS_TIMESTAMPS).setSerializationInclusion(Include.NON_NULL)
        .enable(ACCEPT_SINGLE_VALUE_AS_ARRAY).enable(UNWRAP_SINGLE_VALUE_ARRAYS).disable(FAIL_ON_IGNORED_PROPERTIES);
    mapper.setDateFormat(new SimpleDateFormat(DATE_FORMAT_STR_ISO8601));

    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(Link.class, new LinkSerializer());
    mapper.registerModule(simpleModule);
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return mapper;
  }

}
