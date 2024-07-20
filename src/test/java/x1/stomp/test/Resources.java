package x1.stomp.test;

import jakarta.enterprise.inject.Produces;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import x1.stomp.control.QuickQuote;
import x1.stomp.control.QuickQuoteResult;

public class Resources {
  @Produces
  private Unmarshaller createUnmarshaller() throws Exception {
    var ctx = JAXBContext.newInstance(QuickQuoteResult.class, QuickQuote.class);
    return ctx.createUnmarshaller();
  }
}
