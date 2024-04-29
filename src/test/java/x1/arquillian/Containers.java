package x1.arquillian;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.container.spi.ContainerRegistry;
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
          .withNetworkAliases("postgres").withLogConsumer(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams());

  private final GenericContainer<?> etcd = new GenericContainer<>(DockerImageName.parse("quay.io/coreos/etcd:v3.5.13"))
      .withEnv("ETCD_ENABLE_V2", "true").withNetwork(network).withNetworkAliases("etcd").withCommand("etcd",
          "--listen-client-urls", "http://0.0.0.0:2379", "--advertise-client-urls", "http://etcd:2379");

  private final WildflyContainer wildfly = new WildflyContainer("registry.x1/j7beck/x1-wildfly-stomp-test-it:1.8")
      .dependsOn(database).dependsOn(etcd).withNetwork(network).withEnv("wildfly-testcontainers.properties")
      .withRemoteDebug();

  @Override
  public List<GenericContainer<?>> instances() {
    return Arrays.asList(etcd, database, wildfly);
  }

  @Override
  public void configureAfterStart(ContainerRegistry registry) {
    wildfly.configureAfterStart(registry);
  }

  public static boolean isRemoteArquillian() {
    return System.getProperty("arquillian.launch").equals("remote");
  }

  @Override
  public boolean isActive() {
    return isRemoteArquillian();
  }
}
