package x1.stomp.test;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static x1.stomp.test.ResponseAssert.assertThat;

import java.net.URI;

import static x1.stomp.test.ErrorResponseAssert.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriBuilder;
import x1.stomp.boundary.ErrorResponse;
import x1.stomp.boundary.JacksonConfig;
import x1.stomp.model.Share;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Tag("Testcontainers")
@DisplayName("Testcontainer")
public class ContainerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerTest.class);
    private static final String PATH_SHARES = "shares";
    private static final String PATH_PARAM_KEY = "{key}";
    private static final String PARAM_KEY = "key";
    private static final String TEST_SHARE = "AAPL";
 
    private static final Network network =  Network.newNetwork();
    
    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withNetwork(network).withNetworkAliases("postgres").withDatabaseName("stocks").withInitScript("init.sql");

    @Container
    private static final GenericContainer<?> wildfly = new GenericContainer<>(
        DockerImageName.parse("registry.x1/j7beck/x1-wildfly-stomp-test:1.8")).dependsOn(postgres).withNetwork(network)
            .withEnv("DB_SERVER", "postgres").withEnv("DB_PORT", "5432").withEnv("DB_USER", postgres.getUsername())
            .withEnv("DB_PASSWORD", postgres.getPassword()).withExposedPorts(8080)
            .withLogConsumer(new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams())
            .waitingFor(Wait.forHttp("/").forStatusCode(Status.OK.getStatusCode()));

    private URI baseUrl;
    private Client client;

    @BeforeEach
    void setup() {
      client = ClientBuilder.newClient().register(JacksonConfig.class);
      baseUrl = UriBuilder.fromUri("http://" + wildfly.getHost() + ":" + wildfly.getFirstMappedPort()).path("rest")
          .build();
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

        try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
                .request(APPLICATION_JSON).get()) {
            assertThat(response).hasStatus(NOT_FOUND);
        }
    }

}
