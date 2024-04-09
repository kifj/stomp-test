package x1.arquillian;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import jakarta.ws.rs.core.Response.Status;

public class WildflyContainer extends GenericContainer<WildflyContainer> {
  private static final int HTTP_PORT = 8080;
  private static final int MGMT_PORT = 9990;

  public WildflyContainer() {
    this("registry.x1/j7beck/x1-wildfly-profile:31.0.1.Final");
  }

  public WildflyContainer(String image) {
    super(DockerImageName.parse(image));
  }

  @Override
  protected void configure() {
    withExposedPorts(HTTP_PORT, MGMT_PORT)
        .waitingFor(Wait.forHttp("/health/ready").forPort(MGMT_PORT).forStatusCode(Status.OK.getStatusCode()))
        .withLogConsumer(new SimpleLogConsumer());
  }

  public Integer getManagementPort() {
    return getMappedPort(MGMT_PORT);
  }

  public Integer getHttpPort() {
    return getMappedPort(HTTP_PORT);
  }
}
