package x1.arquillian;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.protocol.ProtocolDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.utility.DockerImageName;

import jakarta.ws.rs.core.Response.Status;

public class WildflyContainer extends GenericContainer<WildflyContainer> {
  private static final Logger LOGGER = LoggerFactory.getLogger(WildflyContainer.class);

  private static final int HTTP_PORT = 8080;
  private static final int MGMT_PORT = 9990;
  private static final int DEBUG_PORT = 8787;

  private static final String JAVA_OPTS = "-server -Xms64m -Xmx512m -XX:MetaspaceSize=96M -XX:MaxMetaspaceSize=256m -XX:+UseG1GC -XX:+UseStringDeduplication -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=org.jboss.byteman -Djava.awt.headless=true";

  public WildflyContainer() {
    this("registry.x1/j7beck/x1-wildfly-profile:32.0.0.Final");
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

  public WildflyContainer withEnv(String file) {
    var p = new Properties();
    try (var is = this.getClass().getClassLoader().getResourceAsStream(file)) {
      p.load(is);
      p.forEach(((key, value) -> this.withEnv(key.toString(), value.toString())));
    } catch (IOException e) {
      LOGGER.warn(e.getMessage());
    }
    return this;
  }

  public WildflyContainer withConfigurationDirectory(String directory) {
    var target = System.getProperty("x1.arquillian.wildfly.configuration", "/srv/wildfly/standalone/configuration");
    LOGGER.info("Source configuration folder {} -> {}", new File(directory).getAbsolutePath(), target);
    for (var source : Objects.requireNonNullElse(new File(directory).listFiles(), new File[0])) {
      LOGGER.debug("Copy {} to {}", source.getAbsolutePath(), target);
      try (var fis = new FileInputStream(source)) {
        var data = IOUtils.toByteArray(fis);
        withCopyToContainer(Transferable.of(data), target + '/' + source.getName());
      } catch (IOException e) {
        LOGGER.warn(e.getMessage());
      }
    }
    return self();
  }

  public WildflyContainer withRemoteDebug() {
    return withRemoteDebug(JAVA_OPTS);
  }

  public WildflyContainer withRemoteDebug(String javaOpts) {
    if (Boolean.getBoolean("arquillian.remote.debug")) {
      var suspend = Boolean.getBoolean("arquillian.remote.debug.suspend") ? "y" : "n";
      LOGGER.info("Enable remote debugging on port {} with suspend={}", DEBUG_PORT, suspend);
      addFixedExposedPort(DEBUG_PORT, DEBUG_PORT);
      withEnv("JAVA_OPTS",
          javaOpts + " -agentlib:jdwp=transport=dt_socket,address=*:" + DEBUG_PORT + ",server=y,suspend=" + suspend);
    }
    return this;
  }

  public void configureAfterStart(ContainerRegistry registry) {
    var arquillianContainer = registry.getContainers().getFirst();
    var containerConfiguration = arquillianContainer.getContainerConfiguration();
    if (Boolean.parseBoolean(System.getProperty("arquillian.useContainerHost", "false"))) {
      containerConfiguration.property("managementAddress", getHost());
    }
    containerConfiguration.property("managementPort", Integer.toString(getManagementPort()));

    // if we would run the test as client, we would need to access the servlet from
    // the host same in Windows we can not access the container network directly
    var protocolConfiguration = arquillianContainer.getProtocolConfiguration(new ProtocolDescription("Servlet 5.0"));
    protocolConfiguration.property("port", Integer.toString(getHttpPort()));
    protocolConfiguration.property("host", System.getProperty("DOCKER_HOST", getHost()));
  }
}
