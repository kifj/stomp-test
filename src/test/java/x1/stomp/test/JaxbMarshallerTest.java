package x1.stomp.test;

import static org.assertj.core.api.Assertions.assertThat;
import static x1.stomp.test.JaxbMarshallerTest.QuickQuoteAssert.assertThat;

import java.util.Objects;

import jakarta.inject.Inject;
import jakarta.xml.bind.Unmarshaller;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import x1.stomp.control.QuickQuote;
import x1.stomp.control.QuickQuoteResult;

public class JaxbMarshallerTest extends AbstractIT {
  @Inject
  private Unmarshaller unmarshaller;
  
  @Test
  void readQuickQuote() throws Exception {
    var is = Objects.requireNonNull(getClass().getClassLoader().getResource("quickquoteresult.xml"));

    var result = (QuickQuoteResult) unmarshaller.unmarshal(is);
    assertThat(result).isNotNull();
    assertThat(result.getQuotes()).hasSize(1);
    var quote = result.getQuotes().getFirst();
    assertThat(quote).hasValues();
  }

  static class QuickQuoteAssert extends AbstractAssert<QuickQuoteAssert, QuickQuote> {

    private QuickQuoteAssert(QuickQuote actual) {
      super(actual, QuickQuoteAssert.class);
    }

    public static QuickQuoteAssert assertThat(QuickQuote actual) {
      return new QuickQuoteAssert(actual);
    }

    @SuppressWarnings("UnusedReturnValue")
    public QuickQuoteAssert hasValues() {
      isNotNull();
      Assertions.assertThat(actual.getCountryCode()).isNotNull();
      Assertions.assertThat(actual.getCurrencyCode()).isNotNull();
      Assertions.assertThat(actual.getExchange()).isNotNull();
      Assertions.assertThat(actual.getLast()).isNotNull();
      Assertions.assertThat(actual.getLastTime()).isNotNull();
      Assertions.assertThat(actual.getName()).isNotNull();
      Assertions.assertThat(actual.getSymbol()).isNotNull();
      Assertions.assertThat(actual.getVolume()).isNotNull();
      return this;
    }
  }

}
