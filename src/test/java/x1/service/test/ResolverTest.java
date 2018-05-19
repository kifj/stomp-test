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

import java.io.File;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

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
        File[] libraries = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.apache.commons:commons-lang3", "io.swagger:swagger-jaxrs", "x1.jboss:service-registry", "org.assertj:assertj-core")
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
        Resolver resolver = new Resolver();

        List<Node> nodes = resolver.resolve(REST, ShareResource.class, VersionData.MAJOR_MINOR, STAGE, HTTPS);
        assertThat(nodes).size().isPositive();
        Node node = getNode(nodes, resolver);
        assertThat(node).isNotNull();
        Properties props = resolver.getProperties(node);
        assertThat(props).size().isEqualTo(5);
        int port = 8443;
        String context = "/" + VersionData.APP_NAME_MAJOR_MINOR;
        URL url = new URL(HTTPS.getPrefix(), hostname, port, context + "/rest/shares");
        assertThat(props.getProperty(BASE_URI)).isEqualTo(url.toString());
        assertThat(props.getProperty(PORT)).isEqualTo(Integer.toString(port));
        assertThat(props.getProperty(CONTEXT)).isEqualTo(context);
        assertThat(props.getProperty(PROTOCOL)).isEqualTo(HTTPS.getPrefix());
        assertThat(props.getProperty(HOST_NAME)).isEqualTo(hostname);
        assertThat(props.getProperty(Constants.DESTINATION)).isNull();
        assertThat(props.getProperty(JNDI_NAME)).isNull();
    }

    @Test
    public void testResolveJms() {
        Resolver resolver = new Resolver();

        List<Node> nodes = resolver.resolve(JMS, ShareMessageListener.class, VersionData.MAJOR_MINOR, STAGE, EJB);
        assertThat(nodes).size().isPositive();
        Node node = getNode(nodes, resolver);
        assertThat(node).isNotNull();
        Properties props = resolver.getProperties(node);
        assertThat(props).size().isEqualTo(5);
        assertThat(props.getProperty(BASE_URI)).isNull();
        String context = "/" + VersionData.APP_NAME_MAJOR_MINOR;
        int port = 8080;
        assertThat(props.getProperty(PORT)).isEqualTo(Integer.toString(port));
        assertThat(props.getProperty(CONTEXT)).isEqualTo(context);
        assertThat(props.getProperty(PROTOCOL)).isEqualTo(EJB.getPrefix());
        assertThat(props.getProperty(HOST_NAME)).isEqualTo(hostname);
        assertThat(props.getProperty(JNDI_NAME)).isEqualTo("java:/jms/queue/stocks");
        assertThat(props.getProperty(Constants.DESTINATION)).isNull();
    }

    private Node getNode(List<Node> nodes, Resolver resolver) {
        for (Node node : nodes) {
            Properties props = resolver.getProperties(node);
            if (props.getProperty(HOST_NAME).equals(hostname)) {
                return node;
            }
        }
        return null;
    }
}
