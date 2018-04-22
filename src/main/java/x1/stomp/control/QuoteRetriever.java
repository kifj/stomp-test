package x1.stomp.control;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.slf4j.Logger;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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

  public Optional<Quote> retrieveQuote(Share share) {
    return createQuote(retrieveQuotes(share.getKey()), share);
  }

  public List<Quote> retrieveQuotes(List<Share> shares) {
    if (shares.isEmpty()) {
      return new ArrayList<>();
    }
    StringBuilder buffer = new StringBuilder();
    shares.forEach(share -> {
      if (buffer.length() > 0) {
        buffer.append("%7C");
      }
      buffer.append(share.getKey());
    });
    return extractQuotes(shares, retrieveQuotes(buffer.toString()));
  }

  private List<Quote> extractQuotes(List<Share> shares, QuickQuoteResult quickQuoteResult) {
    List<Quote> result = new ArrayList<>();
    quickQuoteResult.getQuotes().forEach(quickQuote ->
            createQuote(quickQuote, shares).ifPresent(result::add));
    return result;
  }

  private QuickQuoteResult retrieveQuotes(String keys) {
    log.debug("Retrieve quotes for {}", keys);
    WebTarget target = client.target(URL).queryParam(PARAM_SYMBOLS, keys.toUpperCase()).queryParam(PARAM_OUTPUT,
        VALUE_OUTPUT_JSON);
    QuickQuoteResponse response = target.request(MediaType.APPLICATION_JSON).get(QuickQuoteResponse.class);
    log.debug("Received: {}", response);
    return response.getQuickQuoteResult();
  }

  private Optional<Quote> createQuote(QuickQuote quickQuote, List<Share> shares) {
    if (quickQuote.getLast() == null || quickQuote.getName() == null || quickQuote.getSymbol() == null) {
      return Optional.empty();
    }
    for (Share share : shares) {
      String key = quickQuote.getSymbol();
      if (share.getKey().equalsIgnoreCase(key)) {
        share.setKey(key.toUpperCase());
        share.setName(quickQuote.getName());
        Quote quote = new Quote(share);
        quote.setPrice(quickQuote.getLast());
        quote.setCurrency(StringUtils.defaultString(quickQuote.getCurrencyCode(), DEFAULT_CURRENCY));
        quote.setFrom(quickQuote.getLastTime());
        return Optional.of(quote);
      }
    }
    return Optional.empty();
  }

  private Optional<Quote> createQuote(QuickQuoteResult quickQuoteResult, Share share) {
    List<QuickQuote> quotes = quickQuoteResult.getQuotes();
    if (quotes.isEmpty()) {
      return Optional.empty();
    }
    return createQuote(quotes.get(0), Arrays.asList(share));
  }
}
