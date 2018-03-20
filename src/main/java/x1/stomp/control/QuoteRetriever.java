package x1.stomp.control;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.slf4j.Logger;

import x1.stomp.model.Quote;
import x1.stomp.model.Share;

@ApplicationScoped
public class QuoteRetriever {
  private static final String DEFAULT_CURRENCY = "EUR";

  @Inject
  private Logger log;

  private QuickQuoteService quickQuoteService;

  @Inject
  @ConfigProperty(name = "x1.stomp.control.QuickQuoteService/mp-rest/url")
  private URL baseUrl;

  @PostConstruct
  public void setup() {
    quickQuoteService = RestClientBuilder.newBuilder().baseUrl(baseUrl).build(QuickQuoteService.class);
  }

  public Optional<Quote> retrieveQuote(Share share) {
    return createQuote(retrieveQuotes(share.getKey()), share);
  }

  public List<Quote> retrieveQuotes(List<Share> shares) {
    if (shares.isEmpty()) {
      return new ArrayList<>();
    }
    return extractQuotes(shares, retrieveQuotes(joinKeys(shares)));
  }

  private String joinKeys(List<Share> shares) {
    StringBuilder buffer = new StringBuilder();
    shares.forEach(share -> {
      if (buffer.length() > 0) {
        buffer.append("|");
      }
      buffer.append(share.getKey());
    });
    return buffer.toString();
  }

  private List<Quote> extractQuotes(List<Share> shares, QuickQuoteResult quickQuoteResult) {
    List<Quote> result = new ArrayList<>();
    quickQuoteResult.getQuotes().forEach(quickQuote -> createQuote(quickQuote, shares).ifPresent(result::add));
    return result;
  }

  private QuickQuoteResult retrieveQuotes(String keys) {
    log.debug("Retrieve quotes for {}", keys);
    Response response = quickQuoteService.retrieve(keys.toUpperCase(), "json");
    if (Status.OK == Status.fromStatusCode(response.getStatus())) {
      QuickQuoteResponse quickQuoteResponse = response.readEntity(QuickQuoteResponse.class);
      log.debug("Received: {}", quickQuoteResponse);
      return quickQuoteResponse.getQuickQuoteResult();
    } else {
      throw new WebApplicationException(response);
    }
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
