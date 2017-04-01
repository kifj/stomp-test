package x1.service.test;

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

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

import static org.junit.Assert.*;

import x1.service.Constants;
import x1.service.client.Resolver;
import x1.service.etcd.Node;
import x1.service.registry.Protocol;
import x1.service.registry.Technology;
import x1.stomp.rest.ShareResource;
import x1.stomp.util.VersionData;

@RunWith(Arquillian.class)
public class ResolverTest {
  private static final String STAGE = "local";
  private String hostname;

  @Deployment
  public static Archive<?> createTestArchive() {
    File[] libraries = Maven
        .resolver().loadPomFromFile("pom.xml").resolve("org.apache.httpcomponents:fluent-hc",
            "org.apache.commons:commons-lang3", "io.swagger:swagger-jaxrs", "x1.jboss:service-registry")
        .withTransitivity().asFile();

    return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war").addPackages(true, "x1.stomp")
        .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
        .addAsResource("service-registry.properties").addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsWebInfResource("test-ds.xml").addAsWebInfResource("jboss-deployment-structure.xml")
        .addAsLibraries(libraries);
  }

  @Before
  public void setup() throws Exception {
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostname = "localhost";
    }
  }

  @Test
  public void testResolveHttps() throws Exception {
    Resolver resolver = new Resolver();

    List<Node> nodes = resolver.resolve(Technology.REST, ShareResource.class, VersionData.MAJOR_MINOR, STAGE,
        Protocol.HTTPS);
    assertEquals(1, nodes.size());

    Properties props = resolver.getProperties(nodes.get(0));
    assertEquals(5, props.size());
    int port = 8443;
    String context = "/" + VersionData.APP_NAME_MAJOR_MINOR;
    URL url = new URL(Protocol.HTTPS.getPrefix(), hostname, port, context + "/rest/shares");
    assertEquals(url.toString(), props.getProperty(Constants.BASE_URI));
    assertEquals(Integer.toString(port), props.getProperty(Constants.PORT));
    assertEquals(context, props.getProperty(Constants.CONTEXT));
    assertEquals(Protocol.HTTPS.getPrefix(), props.getProperty(Constants.PROTOCOL));
    assertEquals(hostname, props.getProperty(Constants.HOST_NAME));
    assertNull(props.getProperty(Constants.DESTINATION));
    assertNull(props.getProperty(Constants.JNDI_NAME));
  }

  @Test
  public void testResolveJms() throws Exception {
    Resolver resolver = new Resolver();

    List<Node> nodes = resolver.resolve(Technology.JMS, "x1.stomp.service.ShareMessageListener",
        VersionData.MAJOR_MINOR, STAGE, Protocol.EJB);
    assertEquals(1, nodes.size());

    Properties props = resolver.getProperties(nodes.get(0));
    assertEquals(5, props.size());
    assertNull(props.getProperty(Constants.BASE_URI));
    String context = "/" + VersionData.APP_NAME_MAJOR_MINOR;
    int port = 8080;
    assertEquals(Integer.toString(port), props.getProperty(Constants.PORT));
    assertEquals(context, props.getProperty(Constants.CONTEXT));
    assertEquals(Protocol.EJB.getPrefix(), props.getProperty(Constants.PROTOCOL));
    assertEquals(hostname, props.getProperty(Constants.HOST_NAME));
    assertEquals("java:/jms/queue/stocks", props.getProperty(Constants.JNDI_NAME));
    assertNull(props.getProperty(Constants.DESTINATION));
  }

}
