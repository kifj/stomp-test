package x1.stomp.test;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

import org.apache.commons.io.FileUtils;

import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.service.QuickQuote;
import x1.stomp.service.QuickQuoteResult;
import x1.stomp.util.JsonHelper;

public class JsonHelperTest {

  @Test
  public void testToJson1() throws Exception {
    Command c = new Command();
    c.setAction("foo");
    c.setKey("MSFT");
    String json = JsonHelper.toJSON(c);
    assertEquals("{\"command\":{\"action\":\"foo\",\"key\":\"MSFT\"}}", json);
  }

  @Test
  public void testToJson2() throws Exception {
    Command c = new Command();
    c.setAction("bar");
    String json = JsonHelper.toJSON(c);
    assertEquals("{\"command\":{\"action\":\"bar\"}}", json);
  }

  @Test
  public void testToJson3() throws Exception {
    String json = JsonHelper.toJSON(null);
    assertNull(json);
  }

  @Test
  public void testToJson4() throws Exception {
    Share share = new Share();
    share.setId(1L);
    share.setKey("BMW.DE");
    share.setName("Bayerische Motorenwerke AG");
    Quote q = new Quote();
    q.setCurrency("EUR");
    q.setPrice(1.23f);
    q.setShare(share);
    String json = JsonHelper.toJSON(q);
    assertEquals(
        "{\"Quote\":{\"share\":{\"key\":\"BMW.DE\",\"name\":\"Bayerische Motorenwerke AG\"},\"price\":1.23,\"currency\":\"EUR\"}}",
        json);
  }

  @Test
  public void testFromJson1() throws Exception {
    Command c = JsonHelper.fromJSON("{\"command\":{\"action\":\"foo\",\"key\":\"MSFT\"}}", Command.class);
    assertNotNull(c);
    assertEquals("foo", c.getAction());
    assertEquals("MSFT", c.getKey());
  }

  @Test
  public void testFromJson2() throws Exception {
    Command c = JsonHelper.fromJSON("{\"command\":{\"action\":\"foo\"}}", Command.class);
    assertNotNull(c);
    assertEquals("foo", c.getAction());
    assertNull(c.getKey());
  }

  @Test
  public void testFromJson3() throws Exception {
    Command c = JsonHelper.fromJSON(null, Command.class);
    assertNull(c);
  }

  @Test
  public void testFromJson4() throws Exception {
    File f = new File(getClass().getClassLoader().getResource("quickquoteresult.json").getFile());
    String c = FileUtils.readFileToString(f, "UTF-8");
    QuickQuoteResult q = JsonHelper.fromJSON(c, QuickQuoteResult.class);
    assertNotNull(q);
    assertEquals(2, q.getQuotes().size());
    QuickQuote q1 = q.getQuotes().get(0);
    assertEquals("BMW.DE", q1.getSymbol());
    assertEquals("89.57", q1.getLast().toString());
    assertEquals("EUR", q1.getCurrencyCode());
    assertEquals("DE", q1.getCountryCode());
    assertEquals("Bayerische Motoren Werke AG", q1.getName());
    assertEquals("XETRA", q1.getExchange());
  }
}
