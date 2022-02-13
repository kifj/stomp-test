package x1.stomp.control;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "QuickQuote")
public class QuickQuote implements Serializable {
  private static final long serialVersionUID = -7248251946071933412L;

  private Float last;
  private String name;
  private String symbol;
  private Integer volume;
  private String countryCode;
  private String currencyCode;
  private String exchange;
  private Date lastTime;

  public Float getLast() {
    return last;
  }

  public void setLast(Float last) {
    this.last = last;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSymbol() {
    return symbol;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public Integer getVolume() {
    return volume;
  }

  public void setVolume(Integer volume) {
    this.volume = volume;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public void setCountryCode(String countryCode) {
    this.countryCode = countryCode;
  }

  public String getCurrencyCode() {
    return currencyCode;
  }

  public void setCurrencyCode(String currencyCode) {
    this.currencyCode = currencyCode;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
  }
  
  @JsonProperty("reg_last_time")
  @JsonFormat(shape = Shape.STRING)
  public Date getLastTime() {
    return lastTime;
  }
  
  public void setLastTime(Date lastTime) {
    this.lastTime = lastTime;
  }

  @Override
  public String toString() {
    return "QuickQuote[last=" + last + ", name=" + name + ", symbol=" + symbol + ", volume=" + volume
        + ", countryCode=" + countryCode + ", currencyCode=" + currencyCode + ", exchange=" + exchange + "]";
  }

}
