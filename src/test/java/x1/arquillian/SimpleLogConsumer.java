package x1.arquillian;

import org.testcontainers.containers.output.BaseConsumer;
import org.testcontainers.containers.output.OutputFrame;

public class SimpleLogConsumer extends BaseConsumer<SimpleLogConsumer> {

  @Override
  public void accept(OutputFrame outputFrame) {
    var outputType = outputFrame.getType();
    var utf8String = outputFrame.getUtf8StringWithoutLineEnding();

    switch (outputType) {
      case END -> {}
      case STDOUT -> System.out.println(utf8String);
      case STDERR -> System.err.println(utf8String);
      default -> throw new IllegalArgumentException("Unexpected outputType " + outputType);
    }
  }
}
