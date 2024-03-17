package x1.arquillian;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.event.container.AfterStop;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.core.spi.ServiceLoader;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class TestContainersExtension implements LoadableExtension {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersExtension.class);
  private static final String PACKAGE_NAME = "x1.arquillian";

  public static boolean isRemoteArquillian() {
    return System.getProperty("arquillian.launch").equals("remote");
  }

  @Override
  public void register(ExtensionBuilder builder) {
    if (isRemoteArquillian()) {
      findArquillianTestContainers().ifPresent(containerDefinition -> {
        LoadContainerConfiguration.containerDefinition = containerDefinition;
        builder.observer(LoadContainerConfiguration.class);
      });
    }
  }

  public static final class LoadContainerConfiguration {
    private static ArquillianTestContainers containerDefinition;

    public void registerInstance(@Observes ContainerRegistry registry, ServiceLoader serviceLoader) {
      containerDefinition.instances().forEach(container -> {
        container.start();
        if (containerDefinition.followLog(container)) {
          var logConsumer = containerDefinition.simpleLog(container) ? new SimpleLogConsumer()
              : new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams();
          container.followOutput(logConsumer);
        }
      });
      LOGGER.info("Started {}", getImageNames());
      containerDefinition.configureAfterStart(registry);
    }

    public void stopInstance(@Observes AfterStop event) {
      containerDefinition.instances().forEach(container -> container.stop());
      LOGGER.info("Stopped {}", getImageNames());
    }

    private List<String> getImageNames() {
      return containerDefinition.instances().stream().map(instance -> instance.getDockerImageName())
          .collect(Collectors.toList());
    }
  }

  private Optional<ArquillianTestContainers> findArquillianTestContainers() {
    var classes = new Reflections(PACKAGE_NAME).getTypesAnnotatedWith(ContainerDefinition.class);
    if (classes.isEmpty()) {
      return Optional.empty();
    } else if (classes.size() > 1) {
      throw new IllegalArgumentException(
          "Found more than one ContainerDefinition under " + PACKAGE_NAME + ": " + classes);
    }
    try {
      LOGGER.debug("Found ContainerDefinition in {}", classes);
      return Optional.of((ArquillianTestContainers) classes.iterator().next().getDeclaredConstructor().newInstance());
    } catch (Exception e) {
      LOGGER.warn("Could not create ContainerDefinition", e);
      return Optional.empty();
    }
  }
}
