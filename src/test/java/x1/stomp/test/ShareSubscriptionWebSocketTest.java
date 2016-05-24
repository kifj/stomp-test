package x1.stomp.test;

import java.io.File;
import java.net.URL;

import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import x1.stomp.model.Command;
import x1.stomp.model.SubscriptionEvent;
import x1.stomp.util.JsonHelper;

@RunWith(Arquillian.class)
public class ShareSubscriptionWebSocketTest {
  private String baseUrl;

  @Inject
  private Logger log;

  @ArquillianResource
  private URL url;

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
    if (url == null) {
      baseUrl = "ws://localhost:8080/stomp-test/ws/stocks";
    } else {
      baseUrl = UriBuilder.fromUri(url.toString()).scheme("ws").path("ws/stocks").build().toString();
    }
    log.debug("baseUrl={}", baseUrl);
  }

  @Test
  public void testWebSocket() throws Exception {
    WebSocketClient client = WebSocketClient.openConnection(baseUrl);
    Command command = new Command();
    command.setAction("SUBSCRIBE");
    command.setKey("MSFT");
    String message = JsonHelper.toJSON(command);
    log.debug("Sending {} to {}", command, baseUrl);
    client.sendMessage(message);
    Thread.sleep(2500);

    command.setAction("UNSUBSCRIBE");
    message = JsonHelper.toJSON(command);
    log.debug("Sending {} to {}", command, baseUrl);
    client.sendMessage(message);

    Thread.sleep(2500);
    String response = client.getLastMessage();
    assertNotNull(response);
    SubscriptionEvent event = JsonHelper.fromJSON(response, SubscriptionEvent.class);
    assertEquals("MSFT", event.getKey());
    assertEquals("unsubscribe", event.getAction());
    client.closeConnection();
  }
}
