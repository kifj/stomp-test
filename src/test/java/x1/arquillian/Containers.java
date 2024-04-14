package x1.arquillian;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

@ContainerDefinition
public final class Containers implements ArquillianTestContainers {
  private static final Logger LOGGER = LoggerFactory.getLogger(Containers.class);

  private final Network network = Network.newNetwork();

  private final GenericContainer<?> database = new GenericContainer<>(
      DockerImageName.parse("registry.x1/j7beck/x1-postgres-stomp-test:1.8")).withNetwork(network)
          .withNetworkAliases("db").withLogConsumer(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams());

  private final GenericContainer<?> etcd = new GenericContainer<>(DockerImageName.parse("quay.io/coreos/etcd:v3.5.12"))
      .withEnv("ETCD_ENABLE_V2", "true").withNetwork(network).withNetworkAliases("etcd").withCommand("etcd",
          "--listen-client-urls", "http://0.0.0.0:2379", "--advertise-client-urls", "http://etcd:2379");

  private final WildflyContainer wildfly = new WildflyContainer("registry.x1/j7beck/x1-wildfly-stomp-test-it:1.8")
      .dependsOn(database).dependsOn(etcd).withNetwork(network).withEnv("DB_SERVER", "db").withEnv("DB_PORT", "5432")
      .withEnv("DB_USER", "stocks").withEnv("DB_PASSWORD", "stocks").withEnv("ETCD_SERVER", "etcd")
      .withEnv("ETCD_PORT", "2379").withEnv("X1_SERVICE_REGISTRY_STAGE", "docker");

  private final List<GenericContainer<?>> instances = Arrays.asList(etcd, database, wildfly);

  @Override
  public List<GenericContainer<?>> instances() {
    return instances;
  }

  @Override
  public void configureAfterStart(ContainerRegistry registry) {
    var arquillianContainer = registry.getContainers().getFirst();
    var containerConfiguration = arquillianContainer.getContainerConfiguration();
    if (Boolean.parseBoolean(System.getProperty("arquillian.useContainerHost", "false"))) {
      containerConfiguration.property("managementAddress", wildfly.getHost());
    }
    containerConfiguration.property("managementPort", Integer.toString(wildfly.getManagementPort()));

    // if we would run the test as client, we would need to access the servlet from
    // the host same in Windows we can not access the container network directly
    var protocolConfiguration = arquillianContainer.getProtocolConfiguration(new ProtocolDescription("Servlet 5.0"));
    protocolConfiguration.property("port", Integer.toString(wildfly.getHttpPort()));
    protocolConfiguration.property("host", System.getProperty("DOCKER_HOST", wildfly.getHost()));
  }

  public static boolean isRemoteArquillian() {
    return System.getProperty("arquillian.launch").equals("remote");
  }
  
  @Override
  public boolean isActive() {
    return isRemoteArquillian();
  }
}
