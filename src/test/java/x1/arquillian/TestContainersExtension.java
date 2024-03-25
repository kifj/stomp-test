package x1.arquillian;

import java.util.ArrayList;
import java.util.Collections;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;

public class TestContainersExtension implements LoadableExtension {
  private static final Logger LOGGER = LoggerFactory.getLogger(TestContainersExtension.class);
  private static final String PACKAGE_NAME = "x1.arquillian";

  @Override
  public void register(ExtensionBuilder builder) {
    findArquillianTestContainers().ifPresent(containerDefinition -> {
      LoadContainerConfiguration.containerDefinition = containerDefinition;
      builder.observer(LoadContainerConfiguration.class);
    });
  }

  public static final class LoadContainerConfiguration {
    private static ArquillianTestContainers containerDefinition;

    public void registerInstance(@Observes ContainerRegistry registry, ServiceLoader serviceLoader) {
      containerDefinition.instances().forEach(this::startContainer);
      LOGGER.info("Started {}", getImageNames());
      containerDefinition.configureAfterStart(registry);
    }

    private void startContainer(GenericContainer<?> container) {
      container.start();
      if (containerDefinition.followLog(container)) {
        var logConsumer = containerDefinition.simpleLog(container) ? new SimpleLogConsumer()
            : new Slf4jLogConsumer(LOGGER).withSeparateOutputStreams();
        container.followOutput(logConsumer);
      }
    }

    public void stopInstance(@Observes AfterStop event) {
      reverse(containerDefinition.instances()).forEach(this::stopContainer);
      LOGGER.info("Stopped {}", getImageNames());
    }

    private void stopContainer(GenericContainer<?> container) {
      container.stop();
    }

    private List<GenericContainer<?>> reverse(List<GenericContainer<?>> containers) {
      var reverse = new ArrayList<>(containers);
      Collections.reverse(containers);
      return reverse;
    }

    private List<String> getImageNames() {
      return containerDefinition.instances().stream().map(GenericContainer::getDockerImageName)
          .collect(Collectors.toList());
    }
  }

  private Optional<ArquillianTestContainers> findArquillianTestContainers() {
    var classes = new Reflections(PACKAGE_NAME).getTypesAnnotatedWith(ContainerDefinition.class);
    if (classes.isEmpty()) {
      return Optional.empty();
    } else if (classes.size() > 1) {
      throw new IllegalStateException("Found more than one ContainerDefinition under " + PACKAGE_NAME + ": " + classes);
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
