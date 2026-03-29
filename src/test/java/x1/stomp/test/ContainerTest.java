package x1.stomp.test;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.shaded.org.awaitility.Awaitility;
import org.testcontainers.utility.DockerImageName;
import x1.stomp.boundary.ErrorResponse;
import x1.stomp.boundary.JacksonConfig;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static x1.stomp.test.ErrorResponseAssert.assertThat;
import static x1.stomp.test.ResponseAssert.assertThat;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("Testcontainers")
@DisplayName("Testcontainer")
public class ContainerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerTest.class);
    private static final String PATH_SHARES = "shares";
    private static final String PATH_QUOTES = "quotes";
    private static final String PATH_PARAM_KEY = "{key}";
    private static final String PARAM_KEY = "key";
    private static final String TEST_SHARE = "AAPL";
    private static final String HEADER_CORRELATION_ID = "Correlation-Id";

    private static final Network NETWORK = Network.newNetwork();
    @Container
    private static final KafkaContainer KAFKA = createKafkaContainer();
    @Container
    private static final PostgreSQLContainer POSTGRES = createPostgresSQLContainer();
    @Container
    private static final GenericContainer<?> OTEL = createOtelContainer();
    @Container
    private static final GenericContainer<?> WILDFLY = createWildflyContainer();

    private URI baseUrl;
    private Client client;

    @SuppressWarnings("resource")
    static PostgreSQLContainer createPostgresSQLContainer() {
        try {
            Files.copy(new File("etc/create-postgresql.sql").toPath(), new File("target/test-classes/init.sql").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return new PostgreSQLContainer("postgres:17-alpine").withNetwork(NETWORK).withNetworkAliases("postgres").withDatabaseName("stocks").withInitScript("init.sql");
    }

    @SuppressWarnings("resource")
    static GenericContainer<?> createWildflyContainer() {
        return new GenericContainer<>(DockerImageName.parse("registry.x1/j7beck/x1-wildfly-jar-stomp-test:1.10"))
                .dependsOn(KAFKA)
                .dependsOn(POSTGRES)
                .dependsOn(OTEL)
                .withNetwork(NETWORK)
                .withEnv("DB_SERVER", "postgres")
                .withEnv("DB_PORT", "5432")
                .withEnv("DB_USER", POSTGRES.getUsername())
                .withEnv("DB_PASSWORD", POSTGRES.getPassword())
                .withEnv("MP_MESSAGING_CONNECTOR_SMALLRYE_KAFKA_BOOTSTRAP_SERVERS", "kafka:19092")
                .withEnv("OTEL_COLLECTOR", "otel")
                .withExposedPorts(8080)
                .withLogConsumer(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams())
                .waitingFor(Wait.forHttp("/").forStatusCode(OK.getStatusCode()));
    }

    static KafkaContainer createKafkaContainer() {
        return new KafkaContainer("apache/kafka-native:3.9.2")
                .withNetwork(NETWORK)
                .withNetworkAliases("kafka")
                .withListener("kafka:19092");
    }

    static GenericContainer<?> createOtelContainer() {
        return new GenericContainer<>(DockerImageName.parse("otel/opentelemetry-collector-contrib:0.131.1"))
                .withNetwork(NETWORK)
                .withExposedPorts(4318)
                .waitingFor(Wait.forHttp("/").forStatusCode(NOT_FOUND.getStatusCode()))
                .withNetworkAliases("otel");
    }

    @BeforeEach
    void setup() {
        client = ClientBuilder.newClient().register(JacksonConfig.class);
        baseUrl = UriBuilder.fromUri("http://" + WILDFLY.getHost() + ":" + WILDFLY.getFirstMappedPort()).path("rest").build();
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    @Test
    void testFindShareNotFound() {
        try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY)
                .resolveTemplate(PARAM_KEY, TEST_SHARE).request(APPLICATION_JSON).get()) {
            assertThat(response).hasStatus(NOT_FOUND);
        }
    }

    @Test
    void testAddShareInvalid() {
        var key = "GOOG";
        var share = new Share(key);

        try (var response = client.target(baseUrl).path(PATH_SHARES).request(APPLICATION_JSON)
                .post(Entity.entity(share, MediaType.APPLICATION_XML))) {
            assertThat(response).hasStatus(BAD_REQUEST);
            var errorResponse = response.readEntity(ErrorResponse.class);
            assertThat(errorResponse).isNotNull().containsErrors(2).hasRequestUri().hasType("Invalid data");
        }

        try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY)
                .resolveTemplate(PARAM_KEY, key).request(APPLICATION_JSON).get()) {
            assertThat(response).hasStatus(NOT_FOUND);
        }
    }

    @Test
    void testAddShare() {
        var share = new Share();
        var key = "MSFT";
        var name = "Microsoft Corporation";
        share.setKey(key);
        share.setName(name);

        try (var response = client.target(baseUrl).path(PATH_SHARES).request()
                .header(HEADER_CORRELATION_ID, UUID.randomUUID().toString()).post(Entity.entity(share, APPLICATION_JSON))) {
            assertThat(response).hasStatus(CREATED);
            assertThat(response.getLocation()).isEqualTo(UriBuilder.fromUri(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).build(share.getKey()));
        }

        Awaitility.waitAtMost(20, TimeUnit.SECONDS).with()
                        .pollInterval(Duration.of(1, ChronoUnit.SECONDS))
                                .until(() -> {
                                    try {
                                        var quote = client.target(baseUrl).path(PATH_QUOTES).path(PATH_PARAM_KEY)
                                                .resolveTemplate(PARAM_KEY, share.getKey()).request(APPLICATION_JSON).get(Quote.class);
                                        assertThat(quote).isNotNull();
                                        assertThat(quote.getCurrency()).isNotNull();
                                        assertThat(quote.getPrice()).isNotNull();
                                        assertThat(quote.getShare().getKey()).isEqualTo(share.getKey());
                                        return true;
                                    } catch (Exception e) {
                                        LOGGER.info(e.getMessage());
                                        return false;
                                    }
        });
    }

}
