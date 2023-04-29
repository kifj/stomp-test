package x1.stomp.test;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static x1.stomp.test.ResponseAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;
import static jakarta.ws.rs.core.Response.Status.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.google.gson.JsonParser;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import x1.stomp.model.Share;

@DisplayName("Metrics Test")
public class MetricsTest extends AbstractIT {
  private static final String PATH_SHARES = "shares";
  private String baseUrl;
  private String metricsBaseUrl;

  @BeforeEach
  public void setup() {
    super.setup();
    baseUrl = url.toString() + "rest";
    metricsBaseUrl = getBaseUrlForMetrics();
  }
  
  private String getBaseUrlForMetrics() {
    var host = getHost();
    var port = 9990 + getPortOffset();
    return "http://" + host + ":" + port;
  }
  
  @Inject
  private MeterRegistry registry; 

  @ParameterizedTest
  @ValueSource(strings = { "memory.committedHeap", "memory.committedNonHeap", "memory.maxHeap", "memory.maxNonHeap",
      "memory.usedHeap", "memory.usedNonHeap" })
  @DisplayName("test JVM memory metrics")
  public void testMemoryMetrics(String key) {
    var response = client.target(metricsBaseUrl).path("metrics").path("base").request(APPLICATION_JSON).get();
    assertThat(response).hasStatus(OK);

    var body = response.readEntity(String.class);
    assertThat(body).isNotNull();

    var o = JsonParser.parseString(body).getAsJsonObject();
    assertThat(o).isNotNull();
    assertThat(o.getAsJsonPrimitive(key).getAsLong()).isNotEqualTo(0);
  }

  @Test
  @DisplayName("test health")
  public void testHealth() {
    var response = client.target(metricsBaseUrl).path("health").request(APPLICATION_JSON).get();
    assertThat(response).hasStatus(OK);

    var body = response.readEntity(String.class);
    assertThat(body).isNotNull();

    var o = JsonParser.parseString(body).getAsJsonObject();
    assertThat(o).isNotNull();
    assertThat(o.get("status").getAsString()).isEqualTo("UP");
    var checks = o.getAsJsonArray("checks");
    assertThat(checks).hasSize(7);
  }
  
  @Test
  @DisplayName("test metrics")
  public void testMetrics() {
    var shares = client.target(baseUrl).path(PATH_SHARES).request(APPLICATION_JSON).get(new GenericType<List<Share>>() {
    });
    assertThat(shares).isEmpty();

    var response = client.target(metricsBaseUrl).path("metrics").path("application").request(APPLICATION_JSON).get();
    assertThat(response).hasStatus(OK);

    var body = response.readEntity(String.class);
    assertThat(body).isNotNull();

    var o = JsonParser.parseString(body).getAsJsonObject();
    assertThat(o).isNotNull();
    assertThat(o.getAsJsonObject("add-share").getAsJsonPrimitive("count;interface=ShareResource").getAsInt()).isEqualTo(0);
    assertThat(o.getAsJsonObject("get-share").getAsJsonPrimitive("count;interface=ShareResource").getAsInt()).isEqualTo(0);
    assertThat(o.getAsJsonObject("remove-share").getAsJsonPrimitive("count;interface=ShareResource").getAsInt()).isEqualTo(0);
    assertThat(o.getAsJsonObject("get-shares").getAsJsonPrimitive("count;interface=ShareResource").getAsInt()).isEqualTo(1);
    
    assertThat(registry.timer("add-share", Arrays.asList(Tag.of("interface","ShareResource"))).count()).isEqualTo(0l);
    assertThat(registry.timer("get-share", Arrays.asList(Tag.of("interface","ShareResource"))).count()).isEqualTo(0l);
    assertThat(registry.timer("remove-share", Arrays.asList(Tag.of("interface","ShareResource"))).count()).isEqualTo(0l);
    assertThat(registry.timer("get-shares", Arrays.asList(Tag.of("interface","ShareResource"))).count()).isEqualTo(1l);
  }
}
