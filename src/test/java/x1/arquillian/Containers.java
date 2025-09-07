package x1.arquillian;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@ContainerDefinition
public final class Containers implements ArquillianTestContainers {
    private final Network network = Network.newNetwork();

    @SuppressWarnings("resource")
    private final GenericContainer<?> database = new GenericContainer<>(
            DockerImageName.parse("registry.x1/j7beck/x1-postgres-stomp-test:1.10"))
            .withNetwork(network)
            .withNetworkAliases("postgres")
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(Containers.class)).withSeparateOutputStreams());

    @SuppressWarnings("resource")
    private final GenericContainer<?> etcd = new GenericContainer<>(DockerImageName.parse("quay.io/coreos/etcd:v3.5.21"))
            .withEnv("ETCD_ENABLE_V2", "true").withNetwork(network).withNetworkAliases("etcd").withCommand("etcd",
                    "--listen-client-urls", "http://0.0.0.0:2379", "--advertise-client-urls", "http://etcd:2379");

    private final WildflyContainer wildfly = new WildflyContainer("registry.x1/j7beck/x1-wildfly-stomp-test-it:1.10")
            .dependsOn(database).dependsOn(etcd).withNetwork(network).withEnv("wildfly-testcontainers.properties");

    @SuppressWarnings("resource")
    private final KafkaContainer kafka = new KafkaContainer("apache/kafka-native:3.9.1")
                .withNetwork(network)
                //.withLogConsumer(new Slf4jLogConsumer(LOGGER))
                .withNetworkAliases("kafka")
                .withListener("kafka:19092");

    private final GenericContainer<?> otel =  new GenericContainer<>(DockerImageName.parse("otel/opentelemetry-collector-contrib:0.131.0"))
            .withNetwork(network).withNetworkAliases("otel");

    public static boolean isRemoteArquillian() {
        return System.getProperty("arquillian.launch").equals("remote");
    }

    @Override
    public List<GenericContainer<?>> instances() {
        return List.of(etcd, otel, kafka, database, wildfly);
    }

    @Override
    public void configureAfterStart(ContainerRegistry registry) {
        wildfly.configureAfterStart(registry);
    }

    @Override
    public boolean isActive() {
        return isRemoteArquillian();
    }

}
