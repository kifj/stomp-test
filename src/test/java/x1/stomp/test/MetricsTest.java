package x1.stomp.test;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static x1.stomp.test.ResponseAssert.assertThat;

import java.util.Collection;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.GenericType;
import static jakarta.ws.rs.core.Response.Status.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import com.google.gson.JsonParser;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
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
    assertThat(checks).hasSize(8);
  }
  
  @Test
  @DisplayName("test metrics")
  public void testMetrics() {
    var shares = client.target(baseUrl).path(PATH_SHARES).request(APPLICATION_JSON).get(new Shares());
    assertThat(shares).isEmpty();

    // if no endpoint is configured, Micrometer uses NoOp metrics 
    Collection<Counter> counters = registry.get("rest-request-status").counters();
    assertThat(counters).isNotEmpty();
    assertThat(counters).anyMatch(counter -> counter.getId().getTag("method").equals("listAllShares"));
  }
  
  private static final class Shares extends GenericType<List<Share>> {
  }

}
