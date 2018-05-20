package x1.stomp.test;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import x1.stomp.control.QuickQuoteResult;
import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;

import java.io.File;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static x1.stomp.model.Action.SUBSCRIBE;
import static x1.stomp.model.Action.UNSUBSCRIBE;

public class JsonHelperTest {
  private JsonHelper jsonHelper = new JsonHelper();

  @Test
  public void testToJson1() throws Exception {
    var c = new Command();
    c.setAction(SUBSCRIBE);
    c.setKey("MSFT");
    String json = jsonHelper.toJSON(c);
    assertThat(json).isEqualTo("{\"command\":{\"action\":\"SUBSCRIBE\",\"key\":\"MSFT\"}}");
  }

  @Test
  public void testToJson2() throws Exception {
    var c = new Command();
    c.setAction(UNSUBSCRIBE);
    String json = jsonHelper.toJSON(c);
    assertThat(json).isEqualTo("{\"command\":{\"action\":\"UNSUBSCRIBE\"}}");
  }

  @Test
  public void testToJson3() throws Exception {
    var json = jsonHelper.toJSON(null);
    assertThat(json).isNull();
  }

  @Test
  public void testToJson4() throws Exception {
    var share = new Share();
    share.setId(1L);
    share.setKey("BMW.DE");
    share.setName("Bayerische Motorenwerke AG");
    Quote q = new Quote();
    q.setCurrency("EUR");
    q.setPrice(1.23f);
    q.setShare(share);
    q.setFrom(new Date(123000123000L));
    String json = jsonHelper.toJSON(q);
    assertThat(json)
        .isEqualTo("{\"quote\":{\"share\":{\"key\":\"BMW.DE\",\"name\":\"Bayerische Motorenwerke AG\"},\"price\":1.23,"
            + "\"currency\":\"EUR\",\"from\":\"1973-11-24T14:42:03.000+0000\"}}");
  }

  @Test
  public void testFromJson1() throws Exception {
    var c = jsonHelper.fromJSON("{\"command\":{\"action\":\"UNSUBSCRIBE\",\"key\":\"MSFT\"}}", Command.class);
    assertThat(c).isNotNull();
    assertThat(c.getAction()).isEqualTo(UNSUBSCRIBE);
    assertThat(c.getKey()).isEqualTo("MSFT");
  }

  @Test
  public void testFromJson2() throws Exception {
    var c = jsonHelper.fromJSON("{\"command\":{\"action\":\"SUBSCRIBE\"}}", Command.class);
    assertThat(c).isNotNull();
    assertThat(c.getAction()).isEqualTo(SUBSCRIBE);
    assertThat(c.getKey()).isNull();
  }

  @Test
  public void testFromJson3() throws Exception {
    var c = jsonHelper.fromJSON(null, Command.class);
    assertThat(c).isNull();
  }

  @Test
  public void testFromJson4() throws Exception {
    var f = new File(getClass().getClassLoader().getResource("quickquoteresult.json").getFile());
    var c = FileUtils.readFileToString(f, "UTF-8");
    var q = jsonHelper.fromJSON(c, QuickQuoteResult.class);
    assertThat(q).isNotNull();
    assertThat(q.getQuotes()).size().isEqualTo(2);
    var q1 = q.getQuotes().get(0);
    assertThat(q1.getSymbol()).isEqualTo("BMW.DE");
    assertThat(q1.getLast().toString()).isEqualTo("89.57");
    assertThat(q1.getCurrencyCode()).isEqualTo("EUR");
    assertThat(q1.getCountryCode()).isEqualTo("DE");
    assertThat(q1.getName()).isEqualTo("Bayerische Motoren Werke AG");
    assertThat(q1.getExchange()).isEqualTo("XETRA");
    assertThat(q1.getLastTime()).isNotNull();
  }
}
