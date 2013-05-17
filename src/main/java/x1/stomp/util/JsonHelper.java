package x1.stomp.util;

import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jettison.AbstractXMLStreamReader;
import org.codehaus.jettison.AbstractXMLStreamWriter;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;

public final class JsonHelper {
	private JsonHelper() {
	}

	public static String toJSON(Object o) throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(o.getClass());
		Marshaller marshaller = context.createMarshaller();
		StringWriter sw = new StringWriter();
		MappedNamespaceConvention con = new MappedNamespaceConvention();
		AbstractXMLStreamWriter w = new MappedXMLStreamWriter(con, sw);
		marshaller.marshal(o, w);
		return sw.toString();
	}

	@SuppressWarnings("unchecked")
	public static <T> T fromJSON(String content, Class<? extends T> resultClass) throws Exception {
		JSONObject jsonObject = new JSONObject(content);
		AbstractXMLStreamReader reader = new MappedXMLStreamReader(jsonObject);
		JAXBContext context = JAXBContext.newInstance(resultClass);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		// note: setting schema to null will turn validator off
		unmarshaller.setSchema(null);
		return (T) unmarshaller.unmarshal(reader);
	}
}
