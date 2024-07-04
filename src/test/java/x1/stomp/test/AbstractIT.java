package x1.stomp.test;

import java.net.URL;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import x1.arquillian.Containers;
import x1.stomp.boundary.JacksonConfig;
import x1.stomp.version.VersionData;

@ExtendWith(ArquillianExtension.class)
@Tag("Arquillian")
public abstract class AbstractIT {

  protected Client client;

  @ArquillianResource
  protected URL url;

  @Deployment
  public static Archive<?> createTestArchive() {
    var libraries = Maven
        .resolver().loadPomFromFile("pom.xml").resolve("org.assertj:assertj-core", "org.hamcrest:hamcrest-core",
            "org.testcontainers:activemq", "org.testcontainers:postgresql")
        .withTransitivity().asFile();

    if (Containers.isRemoteArquillian()) {
      return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war")
          .addPackages(true, "x1.stomp").addAsResource("remote-persistence.xml", "META-INF/persistence.xml")
          .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
          .addAsResource("quickquoteresult.xml").addAsWebInfResource("beans.xml")
          .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
    } else {
      return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war")
          .addPackages(true, "x1.stomp").addAsResource("managed-persistence.xml", "META-INF/persistence.xml")
          .addAsWebInfResource("test-ds.xml")
          .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
          .addAsResource("quickquoteresult.xml").addAsWebInfResource("beans.xml")
          .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
    }
  }

  @BeforeEach
  void setup() {
    client = ClientBuilder.newClient().register(JacksonConfig.class);
  }

  @AfterEach
  void tearDown() {
    client.close();
  }

  public Integer getPortOffset() {
    return Integer.valueOf(System.getProperty("jboss.socket.binding.port-offset", "0"));
  }

  public String getHost() {
    return System.getProperty("jboss.bind.address", "127.0.0.1");
  }

}
