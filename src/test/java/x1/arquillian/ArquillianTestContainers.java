package x1.arquillian;

import java.util.Collections;
import java.util.List;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.testcontainers.containers.GenericContainer;

/**
 * implementation must have a no-arg constructor and must be annotated
 * with @ContainerDefinition
 */
public interface ArquillianTestContainers {
  default List<GenericContainer<?>> instances() {
    return Collections.emptyList();
  }

  default void configureAfterStart(ContainerRegistry registry) {
  }
}
