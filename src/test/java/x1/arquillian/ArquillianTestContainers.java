package x1.arquillian;

import java.util.List;

import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.testcontainers.containers.GenericContainer;

/**
 * implementation must have a no-arg constructor and must be annotated
 * with @ContainerDefinition
 */
public interface ArquillianTestContainers {
  List<GenericContainer<?>> instances();

  default void configureAfterStart(ContainerRegistry registry) {
  };

  default boolean followLog(GenericContainer<?> container) {
    return true;
  }

  default boolean simpleLog(GenericContainer<?> container) {
    return false;
  }
}
