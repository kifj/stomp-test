package x1.stomp.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;

import x1.stomp.model.Quote;
import x1.stomp.model.Share;

@ApplicationScoped
public class QuoteRetriever {
  private static final String URL = "https://quote.cnbc.com/quote-html-webservice/quote.htm";
  private static final String DEFAULT_CURRENCY = "EUR";
  private static final String PARAM_OUTPUT = "output";
  private static final String VALUE_OUTPUT_JSON = "json";
  private static final String PARAM_SYMBOLS = "symbols";

  @Inject
  private Logger log;
  private Client client;

  @PostConstruct
  public void setup() {
    client = new ResteasyClientBuilder().establishConnectionTimeout(200, TimeUnit.MILLISECONDS).connectionPoolSize(5)
        .connectionCheckoutTimeout(1, TimeUnit.SECONDS).socketTimeout(2, TimeUnit.SECONDS).build();
  }

  @PreDestroy
  public void tearDown() {
    client.close();
  }

  public Quote retrieveQuote(Share share) {
    try {
      QuickQuoteResult result = retrieveQuotes(share.getKey());
      return createQuote(result, share);
    } catch (IOException e) {
      log.warn("Cound not retrieve quotes for " + share.getKey(), e);
      return null;
    }
  }

  public List<Quote> retrieveQuotes(List<Share> shares) {
    if (shares.isEmpty()) {
      return new ArrayList<>();
    }
    StringBuilder buffer = new StringBuilder();
    for (Share share : shares) {
      if (buffer.length() > 0) {
        buffer.append("%7C");
      }
      buffer.append(share.getKey());
    }
    try {
      QuickQuoteResult result = retrieveQuotes(buffer.toString());
      return extractQuotes(shares, result);
    } catch (IOException e) {
      log.warn("Cound not retrieve quotes for " + buffer, e);
      return new ArrayList<>();
    }
  }

  private List<Quote> extractQuotes(List<Share> shares, QuickQuoteResult quickQuoteResult) {
    List<Quote> result = new ArrayList<>();
    if (quickQuoteResult.getQuotes() != null) {
      for (QuickQuote quickQuote : quickQuoteResult.getQuotes()) {
        Quote quote = createQuote(quickQuote, shares);
        if (quote != null) {
          result.add(quote);
        }
      }
    }
    return result;
  }

  private QuickQuoteResult retrieveQuotes(String keys) throws IOException {
    log.debug("Retrieve quotes for {}", keys);
    WebTarget target = client.target(URL).queryParam(PARAM_SYMBOLS, keys.toUpperCase()).queryParam(PARAM_OUTPUT,
        VALUE_OUTPUT_JSON);
    QuickQuoteResponse response = target.request(MediaType.APPLICATION_JSON).get(QuickQuoteResponse.class);
    log.debug("Received: {}", response);
    return response.getQuickQuoteResult();
  }

  private Quote createQuote(QuickQuote quickQuote, List<Share> shares) {
    if (quickQuote.getLast() == null || quickQuote.getName() == null || quickQuote.getSymbol() == null) {
      return null;
    }
    for (Share share : shares) {
      String key = quickQuote.getSymbol();
      if (share.getKey().equalsIgnoreCase(key)) {
        share.setKey(key);
        share.setName(quickQuote.getName());
        Quote quote = new Quote(share);
        quote.setPrice(quickQuote.getLast());
        quote.setCurrency(StringUtils.defaultString(quickQuote.getCurrencyCode(), DEFAULT_CURRENCY));
        return quote;
      }
    }
    return null;
  }

  private Quote createQuote(QuickQuoteResult quickQuoteResult, Share share) {
    if (quickQuoteResult.getQuotes() != null && quickQuoteResult.getQuotes().isEmpty()) {
      return null;
    }
    return createQuote(quickQuoteResult.getQuotes().get(0), Arrays.asList(share));
  }
}
