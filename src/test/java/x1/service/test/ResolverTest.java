package x1.service.test;

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
import x1.service.Constants;
import x1.service.client.Resolver;
import x1.service.etcd.Node;
import x1.stomp.boundary.ShareResource;
import x1.stomp.control.ShareMessageListener;
import x1.stomp.util.VersionData;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static x1.service.Constants.*;
import static x1.service.registry.Protocol.EJB;
import static x1.service.registry.Protocol.HTTPS;
import static x1.service.registry.Technology.JMS;
import static x1.service.registry.Technology.REST;

@RunWith(Arquillian.class)
public class ResolverTest {
  private static final String STAGE = "local";
  private String hostname;

  @Deployment
  public static Archive<?> createTestArchive() {
    var libraries = Maven.resolver().loadPomFromFile("pom.xml")
        .resolve("io.swagger.core.v3:swagger-jaxrs2", "x1.jboss:service-registry", "org.assertj:assertj-core")
        .withTransitivity().asFile();

    return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war").addPackages(true, "x1.stomp")
        .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
        .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
        .addAsResource("service-registry.properties").addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
        .addAsWebInfResource("test-ds.xml").addAsWebInfResource("jboss-deployment-structure.xml")
        .addAsLibraries(libraries);
  }

  @Before
  public void setup() {
    try {
      hostname = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      hostname = "localhost";
    }
  }

  @Test
  public void testResolveHttps() throws Exception {
    var resolver = new Resolver();

    var nodes = resolver.resolve(REST, ShareResource.class, VersionData.MAJOR_MINOR, STAGE, HTTPS);
    assertThat(nodes).size().isPositive();
    var node = getNode(nodes, resolver);
    assertThat(node).isNotNull();
    var props = resolver.getProperties(node);
    var port = 8443;
    var context = "/" + VersionData.APP_NAME_MAJOR_MINOR;
    var url = new URL(HTTPS.getPrefix(), hostname, port, context + "/rest/shares");
    assertThat(props).containsEntry(BASE_URI, url.toString()).containsEntry(PORT, Integer.toString(port))
        .containsEntry(CONTEXT, context).containsEntry(PROTOCOL, HTTPS.getPrefix()).containsEntry(HOST_NAME, hostname)
        .doesNotContainKeys(Constants.DESTINATION, JNDI_NAME).size().isEqualTo(5);
  }

  @Test
  public void testResolveJms() {
    var resolver = new Resolver();

    var nodes = resolver.resolve(JMS, ShareMessageListener.class, VersionData.MAJOR_MINOR, STAGE, EJB);
    assertThat(nodes).size().isPositive();
    var node = getNode(nodes, resolver);
    assertThat(node).isNotNull();
    var props = resolver.getProperties(node);
    var context = "/" + VersionData.APP_NAME_MAJOR_MINOR;
    var port = 8080;
    assertThat(props).doesNotContainKeys(BASE_URI, Constants.DESTINATION).containsEntry(PORT, Integer.toString(port))
        .containsEntry(CONTEXT, context).containsEntry(PROTOCOL, EJB.getPrefix()).containsEntry(HOST_NAME, hostname)
        .containsEntry(JNDI_NAME, "java:/jms/queue/stocks").size().isEqualTo(5);
  }

  private Node getNode(List<Node> nodes, Resolver resolver) {
    for (var node : nodes) {
      var props = resolver.getProperties(node);
      if (props.getProperty(HOST_NAME).equals(hostname)) {
        return node;
      }
    }
    return null;
  }
}
