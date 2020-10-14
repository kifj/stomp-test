package x1.stomp.test;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static x1.stomp.test.ResponseAssert.assertThat;

import java.net.URL;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import static javax.ws.rs.core.Response.Status.*;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.JsonParser;

import x1.stomp.boundary.JacksonConfig;
import x1.stomp.model.Share;
import x1.stomp.version.VersionData;

@RunWith(Arquillian.class)
public class MetricsTest {
  private static final String PATH_SHARES = "shares";
  
  private String baseUrl;
  private String metricsBaseUrl;
  private Client client;

  @Deployment
  public static Archive<?> createTestArchive() {
    var libraries = Maven.resolver().loadPomFromFile("pom.xml").resolve("org.assertj:assertj-core").withTransitivity()
        .asFile();

    return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war").addPackages(true, "x1.stomp")
        .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
        .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
        .addAsWebInfResource("beans.xml").addAsWebInfResource("test-ds.xml")
        .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
  }

  @ArquillianResource
  private URL url;
  
  @Before
  public void setup() {
    client = ClientBuilder.newClient().register(JacksonConfig.class);
    baseUrl = url.toString() + "rest";
    metricsBaseUrl = getBaseUrlForMetrics();
  }

  @After
  public void teardown() {
    client.close();
  }
  
  private String getBaseUrlForMetrics() {
    var host = getHost();
    var port = 9990 + getPortOffset();
    return "http://" + host + ":" + port;
  }
  
  private Integer getPortOffset() {
    return Integer.valueOf(System.getProperty("jboss.socket.binding.port-offset", "0"));
  }

  private String getHost() {
    return System.getProperty("jboss.bind.address", "127.0.0.1");
  }

  @Test
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
  }

  @Test
  public void testHealth() {
    var response = client.target(metricsBaseUrl).path("health").request(APPLICATION_JSON).get();
    assertThat(response).hasStatus(OK);

    var body = response.readEntity(String.class);
    assertThat(body).isNotNull();

    var o = JsonParser.parseString(body).getAsJsonObject();
    assertThat(o).isNotNull();
    assertThat(o.get("status").getAsString()).isEqualTo("UP");
    var checks = o.getAsJsonArray("checks");
    assertThat(checks).hasSize(6);
  }
}
