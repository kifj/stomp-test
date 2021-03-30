package x1.stomp.test;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.gson.JsonParser;

import x1.stomp.control.QuickQuoteResult;
import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.model.SimpleLink;
import x1.stomp.util.JsonHelper;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static x1.stomp.model.Action.SUBSCRIBE;
import static x1.stomp.model.Action.UNSUBSCRIBE;

@DisplayName("test JSON mapping")
public class JsonHelperTest {
  private final JsonHelper jsonHelper = new JsonHelper();

  private TestData<Command> test(Command object, String json) {
    return new TestData<>(object, json);
  }

  private static class TestData<T> {
    private TestData(T object, String json) {
      this.object = object;
      this.json = json;
    }

    private final T object;
    private final String json;

    protected String getJson() {
      return json;
    }

    protected T getObject() {
      return object;
    }

    @Override
    public String toString() {
      return "TestData [object=" + object + ", json=" + json + "]";
    }
  }

  @TestFactory
  @DisplayName("from Command to JSON")
  Stream<DynamicTest> testCommandToJson() {
    return Stream.of(test(new Command(UNSUBSCRIBE, null), "{\"command\":{\"action\":\"UNSUBSCRIBE\"}}"),
        test(new Command(SUBSCRIBE, "MSFT"), "{\"command\":{\"action\":\"SUBSCRIBE\",\"key\":\"MSFT\"}}"),
        test(null, null)).map(testData -> DynamicTest.dynamicTest(testData.toString(), () -> {
          String json = jsonHelper.toJSON(testData.getObject());
          assertThat(json).isEqualTo(testData.getJson());
        }));
  }

  @Test
  @DisplayName("from Share to JSON")
  void testShareToJson() throws Exception {
    var share = new Share();
    share.setId(1L);
    share.setKey("BMW.DE");
    share.setName("Bayerische Motorenwerke AG");

    var q = new Quote();
    q.setCurrency("EUR");
    q.setPrice(1.23f);
    q.setShare(share);
    q.setFrom(new Date(123000123000L));

    var json = jsonHelper.toJSON(q);
    assertThat(json)
        .isEqualTo("{\"quote\":{\"share\":{\"key\":\"BMW.DE\",\"name\":\"Bayerische Motorenwerke AG\"},\"price\":1.23,"
            + "\"currency\":\"EUR\",\"from\":\"1973-11-24T14:42:03.000+00:00\"}}");
  }

  @TestFactory
  @DisplayName("from JSON to Command")
  Stream<DynamicTest> testJsonToCommand() {
    return Stream
        .of(test(new Command(UNSUBSCRIBE, "MSFT"), "{\"command\":{\"action\":\"UNSUBSCRIBE\",\"key\":\"MSFT\"}}"),
            test(new Command(SUBSCRIBE, null), "{\"command\":{\"action\":\"SUBSCRIBE\"}}"))
        .map(testData -> DynamicTest.dynamicTest(testData.toString(), () -> {
          var c = jsonHelper.fromJSON(testData.getJson(), Command.class);
          Command expected = testData.getObject();
          assertAll(() -> assertThat(c).isNotNull(), () -> assertThat(c.getAction()).isEqualTo(expected.getAction()),
              () -> assertThat(c.getKey()).isEqualTo(expected.getKey()));
        }));
  }

  @Test
  @DisplayName("from NULL to Command")
  void testFromJson3() throws Exception {
    var c = jsonHelper.fromJSON(null, Command.class);
    assertThat(c).isNull();
  }

  @Test
  @DisplayName("from JSON to QuickQuoteResult")
  void testFromJson4() throws Exception {
    var f = new File(getClass().getClassLoader().getResource("quickquoteresult.json").getFile());
    var c = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
    var q = jsonHelper.fromJSON(c, QuickQuoteResult.class);

    assertAll(() -> assertThat(q).isNotNull(), () -> assertThat(q.getQuotes()).size().isEqualTo(2));

    var q1 = q.getQuotes().get(0);
    assertAll(() -> assertThat(q1.getSymbol()).isEqualTo("BMW.DE"),
        () -> assertThat(q1.getLast().toString()).isEqualTo("89.57"),
        () -> assertThat(q1.getCurrencyCode()).isEqualTo("EUR"), () -> assertThat(q1.getCountryCode()).isEqualTo("DE"),
        () -> assertThat(q1.getName()).isEqualTo("Bayerische Motoren Werke AG"),
        () -> assertThat(q1.getExchange()).isEqualTo("XETRA"), () -> assertThat(q1.getLastTime()).isNotNull());
  }

  @DisplayName("from JSON to SimpleLink")
  @Test
  void testSimpleLinkToJson() throws Exception {
    var link = new SimpleLink();
    link.setHref("https://google.com");
    link.setRel("self");
    link.setMethod(HttpMethod.GET);
    link.setTitle("Google");
    link.setType(MediaType.TEXT_HTML);

    var json = jsonHelper.toJSON(link);
    var o = JsonParser.parseString(json).getAsJsonObject().get("SimpleLink").getAsJsonObject();
    assertThat(o).isNotNull();    
    assertThat(o.get("href").getAsString()).isEqualTo("https://google.com");
    assertThat(o.get("rel").getAsString()).isEqualTo("self");
    assertThat(o.get("title").getAsString()).isEqualTo("Google");
    assertThat(o.get("type").getAsString()).isEqualTo(MediaType.TEXT_HTML);
    assertThat(o.get("method").getAsString()).isEqualTo(HttpMethod.GET);
  }
}
