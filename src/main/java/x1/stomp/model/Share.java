package x1.stomp.model;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.core.Link;
import javax.xml.bind.annotation.*;
import javax.validation.constraints.NotEmpty;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;

@Entity
@XmlRootElement(name = "share")
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "share", uniqueConstraints = @UniqueConstraint(columnNames = "key"), indexes = {
    @Index(columnList = "key", name = "idx_key", unique = false),
    @Index(columnList = "name", name = "idx_name", unique = false) })
@NamedQuery(name = "Share.findByKey", query = "from Share s where s.key = :key")
@NamedQuery(name = "Share.listAll", query = "from Share s order by s.name")
@NamedQuery(name = "Share.count", query = "select count(s.id) from Share s")
@Schema(description = "Shares are identified by stock symbols, and may have an name for readability.")
@JsonRootName(value = "share")
public class Share implements Serializable {
  public static final String FIND_BY_KEY = "Share.findByKey";
  public static final String LIST_ALL = "Share.listAll";
  private static final long serialVersionUID = -6219237799499789827L;

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  @JsonIgnore
  @XmlTransient
  private Long id;

  @Version
  @JsonIgnore
  @XmlTransient
  private Long version;

  @NotNull
  @Size(min = 1, max = 25)
  @Pattern(regexp = "[A-Z0-9.]*", message = "must contain only letters and dots")
  @Column
  @Schema(required = true, description = "Stock symbol", example = "MSFT")
  @XmlAttribute
  private String key;

  @NotNull
  @NotEmpty
  @Size(min = 1, max = 80)
  @Column(length = 80)
  @Schema(required = false, description = "Human readable name", example = "Microsoft Corp.")
  @XmlAttribute
  private String name;

  @JsonProperty(value = "links")
  @XmlElement(name = "link")
  @XmlJavaTypeAdapter(Link.JaxbAdapter.class)
  @Transient
  @ArraySchema(schema = @Schema(implementation = SimpleLink.class, accessMode = AccessMode.READ_ONLY))
  private List<Link> links;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long version) {
    this.version = version;
  }

  public List<Link> getLinks() {
    return links;
  }

  public void setLinks(List<Link> links) {
    this.links = links;
  }

  @Override
  public String toString() {
    return "Share[id=" + id + ", key=" + key + ", name=" + name + "]";
  }
}