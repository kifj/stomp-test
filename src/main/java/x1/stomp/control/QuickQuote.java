package x1.stomp.control;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "QuickQuote")
@XmlRootElement(name = "QuickQuote")
public class QuickQuote implements Serializable {
  private static final long serialVersionUID = -7248251946071933412L;

  private Float last;
  private String name;
  private String symbol;
  private Integer volume;
  private String countryCode;
  private String currencyCode;
  private String exchange;
  private Date timestamp;

  @XmlElement(name = "last")
  public Float getLast() {
    return last;
  }

  public void setLast(Float last) {
    this.last = last;
  }

  @XmlElement(name = "name")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlElement(name = "symbol")
  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  @XmlElement(name = "volume")
  public Integer getVolume() {
    return volume;
  }

  public void setVolume(Integer volume) {
    this.volume = volume;
  }

  @XmlElement(name = "countryCode")
  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  @XmlElement(name = "currencyCode")
  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  @XmlElement(name = "exchange")
  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }

  @JsonProperty("reg_last_time")
  @JsonFormat(shape = Shape.STRING)
  @XmlElement(name = "reg_last_time")
  public Date getLastTime() {
    return timestamp;
  }

  public void setLastTime(Date lastTime) {
    this.timestamp = lastTime;
  }

  @Override
  public String toString() {
    return "QuickQuote [last=" + last + ", name=" + name + ", symbol=" + symbol + ", volume=" + volume
        + ", countryCode=" + countryCode + ", currencyCode=" + currencyCode + ", exchange=" + exchange + ", timestamp="
        + timestamp + "]";
  }
  
}
