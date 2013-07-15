package x1.stomp.util;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

public final class JsonHelper {
  private JsonHelper() {
  }

  public static String toJSON(Object o) throws JAXBException {
    if (o == null) {
      return null;
    }
    JAXBContext context = JAXBContext.newInstance(o.getClass());
    Marshaller marshaller = context.createMarshaller();
    StringWriter sw = new StringWriter();
    MappedNamespaceConvention con = new MappedNamespaceConvention();
    AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, sw);
    marshaller.marshal(o, w);
    return sw.toString();
  }

  @SuppressWarnings("unchecked")
  public static <T> T fromJSON(String content, Class<? extends T> resultClass) throws IOException {
    if (StringUtils.isEmpty(content)) {
      return null;
    }
    try {
      JSONObject jsonObject = new JSONObject(content);
      AbstractXMLStreamReader reader = new MappedXMLStreamReader(jsonObject);
      JAXBContext context = JAXBContext.newInstance(resultClass);
      Unmarshaller unmarshaller = context.createUnmarshaller();
      // note: setting schema to null will turn validator off
      unmarshaller.setSchema(null);
      return (T) unmarshaller.unmarshal(reader);
    } catch (JAXBException | JSONException | XMLStreamException e) {
      throw new IOException(e);
    }
  }
}
