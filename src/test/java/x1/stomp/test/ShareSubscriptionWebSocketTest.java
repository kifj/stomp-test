package x1.stomp.test;

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
import x1.stomp.control.QuoteUpdater;
import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.model.SubscriptionEvent;
import x1.stomp.util.JsonHelper;

import javax.ejb.EJB;
import javax.inject.Inject;
import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static x1.stomp.model.Action.SUBSCRIBE;
import static x1.stomp.model.Action.UNSUBSCRIBE;

@RunWith(Arquillian.class)
public class ShareSubscriptionWebSocketTest {
  private static final String TEST_SHARE = "MSFT";

  private String baseUrl;

  @Inject
  private Logger log;

  @Inject
  private JsonHelper jsonHelper;

  @EJB
  private QuoteUpdater quoteUpdater;

  @Deployment
  public static Archive<?> createTestArchive() {
    File[] libraries = Maven.resolver().loadPomFromFile("pom.xml")
            .resolve("org.apache.commons:commons-lang3", "io.swagger:swagger-jaxrs")
            .withTransitivity().asFile();

    return ShrinkWrap.create(WebArchive.class, "stomp-test.war").addPackages(true, "x1.stomp")
            .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
            .addAsResource("META-INF/microprofile-config.properties", "META-INF/microprofile-config.properties")            
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsWebInfResource("test-ds.xml")
            .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
  }

  @Before
  public void setup() {
    baseUrl = "ws://" + System.getProperty("jboss.bind.address", "127.0.0.1") + ":8080/stomp-test/ws/stocks";
    log.debug("baseUrl={}", baseUrl);
  }

  @Test
  public void testWebSocket() throws Exception {
    WebSocketClient client = WebSocketClient.openConnection(baseUrl);
    Command command = new Command(SUBSCRIBE, TEST_SHARE);
    String message = jsonHelper.toJSON(command);
    log.debug("Sending {} to {}", command, baseUrl);
    client.sendMessage(message);
    Thread.sleep(2500);

    Quote quote = new Quote();
    quote.setCurrency("USD");
    quote.setPrice(10.0f);
    Share share = new Share();
    share.setKey(TEST_SHARE);
    quote.setShare(share);
    quoteUpdater.updateQuote(quote);
    Thread.sleep(1500);
    String response = client.getLastMessage();
    assertNotNull(response);
    log.debug("Received: {}", response);
    Quote received = jsonHelper.fromJSON(response, Quote.class);
    assertEquals(quote.getPrice(), received.getPrice());
    assertEquals(quote.getCurrency(), received.getCurrency());
    assertEquals(TEST_SHARE, received.getShare().getKey());

    command.setAction(UNSUBSCRIBE);
    message = jsonHelper.toJSON(command);
    log.debug("Sending {} to {}", command, baseUrl);
    client.sendMessage(message);

    Thread.sleep(2500);
    response = client.getLastMessage();
    assertNotNull(response);
    SubscriptionEvent event = jsonHelper.fromJSON(response, SubscriptionEvent.class);
    assertEquals(TEST_SHARE, event.getKey());
    assertEquals(UNSUBSCRIBE, event.getAction());
    client.closeConnection();
  }
}
