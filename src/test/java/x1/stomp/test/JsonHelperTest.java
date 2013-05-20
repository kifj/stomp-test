package x1.stomp.test;

import org.junit.Test;
import static org.junit.Assert.*;
import x1.stomp.model.Command;
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
}
