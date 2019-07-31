package x1.stomp.test;

import static x1.stomp.model.Action.SUBSCRIBE;
import static x1.stomp.model.Action.UNSUBSCRIBE;

import javax.ejb.EJB;
import javax.inject.Inject;

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
import x1.stomp.util.VersionData;

import static org.assertj.core.api.Assertions.assertThat;

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

  @Inject
  private WebSocketClient client;

  @Deployment
  public static Archive<?> createTestArchive() {
    var libraries = Maven.resolver().loadPomFromFile("pom.xml")
        .resolve("io.swagger.core.v3:swagger-jaxrs2", "org.assertj:assertj-core").withTransitivity().asFile();

    return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war").addPackages(true, "x1.stomp")
        .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
        .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsWebInfResource("test-ds.xml")
        .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
  }

  @Before
  public void setup() {
    var host = System.getProperty("jboss.bind.address", "127.0.0.1");
    var port = 8080 + Integer.parseInt(System.getProperty("jboss.socket.binding.port-offset", "0"));
    baseUrl = "ws://" + host + ":" + port + "/" + VersionData.APP_NAME_MAJOR_MINOR + "/ws/stocks";
    log.debug("baseUrl={}", baseUrl);
  }

  @Test
  public void testWebSocket() throws Exception {
    client.openConnection(baseUrl);
    Thread.sleep(500);
    var command = new Command(SUBSCRIBE, TEST_SHARE);
    var message = jsonHelper.toJSON(command);
    log.debug("Sending {} to {}", command, baseUrl);
    client.sendMessage(message);
    Thread.sleep(2500);

    var quote = new Quote();
    quote.setCurrency("USD");
    quote.setPrice(10.0f);
    var share = new Share();
    share.setKey(TEST_SHARE);
    quote.setShare(share);
    quoteUpdater.updateQuote(quote);
    Thread.sleep(1500);
    var response = client.getLastMessage();
    assertThat(response).isNotNull();
    log.debug("Received: {}", response);
    var received = jsonHelper.fromJSON(response, Quote.class);
    assertThat(received.getPrice()).isEqualTo(quote.getPrice());
    assertThat(received.getCurrency()).isEqualTo(quote.getCurrency());
    assertThat(received.getShare().getKey()).isEqualTo(TEST_SHARE);

    command.setAction(UNSUBSCRIBE);
    message = jsonHelper.toJSON(command);
    log.debug("Sending {} to {}", command, baseUrl);
    client.sendMessage(message);

    Thread.sleep(2500);
    response = client.getLastMessage();
    assertThat(response).isNotNull();
    SubscriptionEvent event = jsonHelper.fromJSON(response, SubscriptionEvent.class);
    assertThat(event.getKey()).isEqualTo(TEST_SHARE);
    assertThat(event.getAction()).isEqualTo(UNSUBSCRIBE);
    client.closeConnection();
  }

}
