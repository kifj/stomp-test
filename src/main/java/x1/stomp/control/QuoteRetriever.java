package x1.stomp.control;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

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
    StringJoiner sj = new StringJoiner("|");
    shares.forEach(share -> sj.add(share.getKey()));
    return sj.toString();
  }

  private List<Quote> extractQuotes(List<Share> shares, QuickQuoteResult quickQuoteResult) {
    return quickQuoteResult.getQuotes().stream().map(quickQuote -> createQuote(quickQuote, shares))
        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
  }

  private QuickQuoteResult retrieveQuotes(String keys) {
    try {
      log.debug("Retrieve quotes for {}", keys);
      Response response = quickQuoteService.retrieve(keys.toUpperCase(), "json");

      QuickQuoteResponse quickQuoteResponse = response.readEntity(QuickQuoteResponse.class);
      log.debug("Received: {}", quickQuoteResponse);
      return quickQuoteResponse.getQuickQuoteResult();
    } catch (RuntimeException e) {
      log.error(e.getMessage());
      throw e;
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
    return createQuote(quotes.get(0), Collections.singletonList(share));
  }
}
