package x1.stomp.model;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.xml.namespace.QName;

import jakarta.ws.rs.core.Link;
import jakarta.xml.bind.annotation.XmlAnyAttribute;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

/**
 * copied from jakarta.ws.rs.core where this is deprecated 
 */
public final class JaxbSupport {
  private JaxbSupport() {}
  
  public static class JaxbLink {

    private URI uri;
    private Map<QName, Object> params;

    public JaxbLink() {
    }

    public JaxbLink(final URI uri) {
      this.uri = uri;
    }

    public JaxbLink(final URI uri, final Map<QName, Object> params) {
      this.uri = uri;
      this.params = params;
    }

    @XmlAttribute(name = "href")
    public URI getUri() {
      return uri;
    }

    @XmlAnyAttribute
    public Map<QName, Object> getParams() {
      if (params == null) {
        params = new HashMap<>();
      }
      return params;
    }

    void setUri(final URI uri) {
      this.uri = uri;
    }

    void setParams(final Map<QName, Object> params) {
      this.params = params;
    }

    @Override
    public boolean equals(final Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof JaxbLink jaxbLink)) {
        return false;
      }

      if (!Objects.equals(uri, jaxbLink.uri)) {
        return false;
      }

      if (params == jaxbLink.params) {
        return true;
      }
      if (params == null) {
        // if this.params is 'null', consider other.params equal to empty
        return jaxbLink.params.isEmpty();
      }
      if (jaxbLink.params == null) {
        // if other.params is 'null', consider this.params equal to empty
        return params.isEmpty();
      }

      return params.equals(jaxbLink.params);
    }

    @Override
    public int hashCode() {
      return Objects.hash(uri, params);
    }

  }
  
  public static class JaxbAdapter extends XmlAdapter<JaxbLink, Link> {

    @Override
    public Link unmarshal(final JaxbLink v) {
        var lb = Link.fromUri(v.getUri());
        for (var e : v.getParams().entrySet()) {
            lb.param(e.getKey().getLocalPart(), e.getValue().toString());
        }
        return lb.build();
    }

    @Override
    public JaxbLink marshal(final Link v) {
        var jl = new JaxbLink(v.getUri());
        for (var e : v.getParams().entrySet()) {
            jl.getParams().put(new QName("", e.getKey()), e.getValue());
        }
        return jl;
    }
}
}
