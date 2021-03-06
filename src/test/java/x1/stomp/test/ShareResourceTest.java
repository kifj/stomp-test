package x1.stomp.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import x1.stomp.boundary.ErrorResponse;
import x1.stomp.boundary.JacksonConfig;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.version.VersionData;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import java.net.URL;
import java.util.List;
import java.util.UUID;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.*;
import static x1.stomp.test.ResponseAssert.assertThat;
import static x1.stomp.test.ErrorResponseAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(ArquillianExtension.class)
@DisplayName("ShareResource Test")
public class ShareResourceTest {
  private static final String HEADER_CORRELATION_ID = "Correlation-Id";
  private static final String PATH_QUOTES = "quotes";
  private static final String PATH_SHARES = "shares";
  private static final String PATH_PARAM_KEY = "{key}";
  private static final String PARAM_KEY = "key";
  private static final String TEST_SHARE = "AAPL";
  private static final String TEST_SHARE_INVALID = "XXXX";

  private String baseUrl;
  private Client client;

  @ArquillianResource
  private URL url;

  @Deployment
  public static Archive<?> createTestArchive() {
    var libraries = Maven.resolver().loadPomFromFile("pom.xml")
        .resolve("org.assertj:assertj-core", "org.hamcrest:hamcrest-library").withTransitivity().asFile();

    return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war").addPackages(true, "x1.stomp")
        .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
        .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
        .addAsWebInfResource("beans.xml").addAsWebInfResource("test-ds.xml")
        .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
  }

  @BeforeEach
  public void setup() {
    baseUrl = url.toString() + "rest";
    client = ClientBuilder.newClient().register(JacksonConfig.class);
  }

  @AfterEach
  public void teardown() {
    client.close();
  }

  @Test
  public void testFindShareNotFound() {
    try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY)
        .resolveTemplate(PARAM_KEY, TEST_SHARE).request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  @Test
  public void testAddAndFindShare() throws Exception {
    var share = new Share();
    var key = "BMW";
    var name = "Bayerische Motoren Werke AG";
    share.setKey(key);
    share.setName(name);

    try (var response = client.target(baseUrl).path(PATH_SHARES).request()
        .header(HEADER_CORRELATION_ID, UUID.randomUUID().toString()).post(Entity.entity(share, APPLICATION_JSON))) {
      assertThat(response).hasStatus(CREATED);
      assertThat(response.getLocation())
          .isEqualTo(UriBuilder.fromUri(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).build(share.getKey()));
    }

    var loop = 0;
    while (true) {
      try {
        var found = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
            .request(APPLICATION_JSON).get(Share.class);
        assertThat(found).isNotNull();
        assertThat(found.getId()).isNull();
        assertThat(found.getKey()).isEqualTo(key);
        assertThat(found.getLinks()).hasSize(3);
        found.getLinks().forEach(link -> {
          assertThat(link.getUri()).isNotNull();
          assertThat(link.getRel()).isNotNull();
        });
        break;
      } catch (NotFoundException e) {
        Thread.sleep(500);
        loop++;
        if (loop == 20) {
          fail(e.getMessage());
        }
      }
    }

    var shares = client.target(baseUrl).path(PATH_SHARES).request(APPLICATION_JSON).get(new GenericType<List<Share>>() {
    });
    assertThat(shares).size().isEqualTo(1);

    var quote = client.target(baseUrl).path(PATH_QUOTES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, share.getKey())
        .request(APPLICATION_JSON).get(Quote.class);
    assertThat(quote).isNotNull();
    assertThat(quote.getCurrency()).isNotNull();
    assertThat(quote.getPrice()).isNotNull();
    assertThat(quote.getShare().getKey()).isEqualTo(share.getKey());

    var quotes = client.target(baseUrl).path(PATH_QUOTES).queryParam(PARAM_KEY, share.getKey(), TEST_SHARE_INVALID)
        .request(APPLICATION_JSON).get(new GenericType<List<Quote>>() {
        });
    assertThat(quotes).size().isEqualTo(1);

    try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY)
        .resolveTemplate(PARAM_KEY, share.getKey()).request(APPLICATION_JSON).delete()) {
      assertThat(response).hasStatus(OK);
    }

    try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
        .request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  @Test
  public void testAddShareInvalid() {
    var key = "GOOG";
    var share = new Share(key);

    try (var response = client.target(baseUrl).path(PATH_SHARES).request(APPLICATION_JSON)
        .post(Entity.entity(share, MediaType.APPLICATION_XML))) {
      assertThat(response).hasStatus(BAD_REQUEST);
      var errorResponse = response.readEntity(ErrorResponse.class);
      assertThat(errorResponse).isNotNull().containsErrors(2).hasRequestUri().hasType("Invalid data");
    }

    try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
        .request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  @Test
  public void testGetQuoteNotFound() {
    try (var response = client.target(baseUrl).path(PATH_QUOTES).path(PATH_PARAM_KEY)
        .resolveTemplate(PARAM_KEY, TEST_SHARE).request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  @Test
  public void testGetQuotesNotFound() {
    try (var response = client.target(baseUrl).path(PATH_QUOTES).queryParam(PARAM_KEY, TEST_SHARE, TEST_SHARE_INVALID)
        .request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

}
