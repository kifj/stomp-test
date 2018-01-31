package x1.stomp.test;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import static org.junit.Assert.*;

import javax.inject.Inject;

import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.rest.ErrorResponse;

@RunWith(Arquillian.class)
public class ShareResourceTest {
  private static final String HEADER_CORRELATION_ID = "Correlation-Id";
  private static final String PATH_QUOTES = "quotes";
  private static final String PATH_SHARES = "shares";
  private static final String PATH_PARAM_KEY = "{key}";
  private static final String PARAM_KEY = "key";
  public static final String TEST_SHARE = "AAPL";

  private String baseUrl;

  @Inject
  private Logger log;

  @Deployment
  public static Archive<?> createTestArchive() {
    File[] libraries = Maven.resolver().loadPomFromFile("pom.xml")
        .resolve("org.apache.httpcomponents:fluent-hc", "org.apache.commons:commons-lang3", "io.swagger:swagger-jaxrs")
        .withTransitivity().asFile();

    return ShrinkWrap.create(WebArchive.class, "stomp-test.war").addPackages(true, "x1.stomp")
        .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsWebInfResource("test-ds.xml")
        .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
  }

  @Before
  public void setup() {
    baseUrl = "http://" + System.getProperty("jboss.bind.address", "127.0.0.1") + ":8080/stomp-test/rest";
    log.debug("baseUrl={}", baseUrl);
  }

  @Test
  public void testFindShareNotFound() throws Exception {
    Client client = ClientBuilder.newClient();
    Response response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, TEST_SHARE)
        .request(MediaType.APPLICATION_JSON).get();
    assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
    response.close();
  }

  @Test
  public void testAddAndFindShare() throws Exception {
    Share share = new Share();
    String key = "BMW.DE";
    String name = "Bayerische Motoren Werke AG";
    share.setKey(key);
    share.setName(name);

    Client client = ClientBuilder.newClient();
    Response resp = client.target(baseUrl).path(PATH_SHARES).request()
        .header(HEADER_CORRELATION_ID, UUID.randomUUID().toString())
        .post(Entity.entity(share, MediaType.APPLICATION_JSON));
    assertNotNull(resp);
    assertEquals(Status.CREATED.getStatusCode(), resp.getStatus());
    assertEquals(UriBuilder.fromUri(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).build(share.getKey()).toString(),
        resp.getLocation().toString());
    resp.close();
    Thread.sleep(10000);
    Share found = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
        .request(MediaType.APPLICATION_JSON).get(Share.class);
    assertNotNull(found);
    assertNull(found.getId());
    assertEquals(key, found.getKey());

    List<Share> shares = client.target(baseUrl).path(PATH_SHARES).request(MediaType.APPLICATION_JSON)
        .get(new GenericType<List<Share>>() {
        });
    assertEquals(1, shares.size());

    Quote quote = client.target(baseUrl).path(PATH_QUOTES).path(PATH_PARAM_KEY)
        .resolveTemplate(PARAM_KEY, share.getKey()).request(MediaType.APPLICATION_JSON).get(Quote.class);
    assertNotNull(quote);
    assertNotNull(quote.getCurrency());
    assertNotNull(quote.getPrice());
    assertEquals(quote.getShare().getKey(), share.getKey());

    Response response3 = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY)
        .resolveTemplate(PARAM_KEY, share.getKey()).request(MediaType.APPLICATION_JSON).delete();
    assertEquals(Status.OK.getStatusCode(), response3.getStatus());
  }

  @Test
  public void testAddShareInvalid() throws Exception {
    Share share = new Share();
    String key = "GOOG";
    share.setKey(key);
    Client client = ClientBuilder.newClient();
    Response response = client.target(baseUrl).path(PATH_SHARES).request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(share, MediaType.APPLICATION_XML));
    assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
    ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
    assertNotNull(errorResponse);
    assertEquals(2, errorResponse.getErrors().size());

    Response response2 = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
        .request(MediaType.APPLICATION_JSON).get();
    assertEquals(Status.NOT_FOUND.getStatusCode(), response2.getStatus());
  }

  @Test
  public void testGetQuoteNotFound() throws Exception {
    Client client = ClientBuilder.newClient();
    Response response = client.target(baseUrl).path(PATH_QUOTES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, TEST_SHARE)
        .request(MediaType.APPLICATION_JSON).get();
    assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

}
