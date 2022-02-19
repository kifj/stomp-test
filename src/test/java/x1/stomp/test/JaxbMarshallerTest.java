package x1.stomp.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import javax.xml.bind.JAXBContext;

import org.junit.jupiter.api.Test;

import x1.stomp.control.QuickQuote;
import x1.stomp.control.QuickQuoteResult;

public class JaxbMarshallerTest extends AbstractIT {

  @Test
  public void readQuickQuote() throws Exception {
    var ctx = JAXBContext.newInstance(QuickQuoteResult.class, QuickQuote.class);
    var unmarshaller = ctx.createUnmarshaller();
    var is = Objects.requireNonNull(getClass().getClassLoader().getResource("quickquoteresult.xml"));

    var result = (QuickQuoteResult) unmarshaller.unmarshal(is);
    assertThat(result).isNotNull();
    assertThat(result.getQuotes()).hasSize(1);
    var quote = result.getQuotes().get(0);
    assertThat(quote).isNotNull();
    assertThat(quote.getCountryCode()).isNotNull();
    assertThat(quote.getCurrencyCode()).isNotNull();
    assertThat(quote.getExchange()).isNotNull();
    assertThat(quote.getLast()).isNotNull();
    assertThat(quote.getLastTime()).isNotNull();
    assertThat(quote.getName()).isNotNull();
    assertThat(quote.getSymbol()).isNotNull();
    assertThat(quote.getVolume()).isNotNull();
  }

}
