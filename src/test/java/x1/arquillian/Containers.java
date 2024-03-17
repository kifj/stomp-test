package x1.arquillian;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import jakarta.ws.rs.core.Response.Status;

@ContainerDefinition
public final class Containers implements ArquillianTestContainers {
  private Network network = Network.newNetwork();
  
  private GenericContainer<?> database = new GenericContainer<>(
      DockerImageName.parse("registry.x1/j7beck/x1-postgres-stomp-test:1.8")).withNetwork(network)
          .withNetworkAliases("db");
  
  private GenericContainer<?> etcd = new GenericContainer<>(DockerImageName.parse("quay.io/coreos/etcd:v3.5.11"))
      .withEnv("ETCD_ENABLE_V2", "true").withNetwork(network).withNetworkAliases("etcd").withCommand("etcd",
          "--listen-client-urls", "http://0.0.0.0:2379", "--advertise-client-urls", "http://etcd:2379");

  private GenericContainer<?> wildfly = new GenericContainer<>(
      DockerImageName.parse("registry.x1/j7beck/x1-wildfly-stomp-test-it:1.8")).dependsOn(database).dependsOn(etcd)
          .withNetwork(network).withEnv("DB_SERVER", "db").withEnv("DB_PORT", "5432").withEnv("DB_USER", "stocks")
          .withEnv("DB_PASSWORD", "stocks").withEnv("ETCD_SERVER", "etcd").withEnv("ETCD_PORT", "2379")
          .withEnv("X1_SERVICE_REGISTRY_STAGE", "docker").withExposedPorts(8080, 9990)
          .waitingFor(Wait.forHttp("/health/ready").forStatusCode(Status.OK.getStatusCode()));

  private final List<GenericContainer<?>> instances = Arrays.asList(etcd, database, wildfly);

  @Override
  public List<GenericContainer<?>> instances() {
    return instances;
  }
  
  @Override
  public void configureAfterStart(ContainerRegistry registry) {
    Container arquillianContainer = registry.getContainers().iterator().next();
    ContainerDef containerConfiguration = arquillianContainer.getContainerConfiguration();
    containerConfiguration.property("managementPort", Integer.toString(wildfly.getMappedPort(9990)));

    // if we would run the test as client, we would need to access the servlet from the host
    // ProtocolDef protocolConfiguration = arquillianContainer.getProtocolConfiguration(new ProtocolDescription(ServletProtocolDefinition.NAME));
    // protocolConfiguration.property("port", Integer.toString(wildfly.getMappedPort(8080)));
  }

  @Override
  public boolean followLog(GenericContainer<?> container) {
    if (container == etcd) {
      return false;
    }
    return true;
  }

  @Override
  public boolean simpleLog(GenericContainer<?> container) {
    if (container == wildfly) {
      return true;
    }
    return false;
  }

}
