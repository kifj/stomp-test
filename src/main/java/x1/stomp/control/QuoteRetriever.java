package x1.stomp.control;

import java.util.*;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.slf4j.Logger;

import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.Logged;

@ApplicationScoped
public class QuoteRetriever {
  private static final String DEFAULT_CURRENCY = "EUR";

  @Inject
  private Logger log;

  @Inject
  private QuickQuoteService quickQuoteService;
  
  @Timed(name = "retrieve-quote-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  @Logged
  public Optional<Quote> retrieveQuote(Share share) {
    return createQuote(retrieveQuotes(share.getKey()), share);
  }

  @Timed(name = "retrieve-quotes-timer", absolute = true, unit = MetricUnits.MILLISECONDS)
  @Logged
  public List<Quote> retrieveQuotes(List<Share> shares) {
    if (shares.isEmpty()) {
      return new ArrayList<>();
    }
    return extractQuotes(shares, retrieveQuotes(joinKeys(shares)));
  }

  private String joinKeys(List<Share> shares) {
    return shares.stream().map(Share::getKey).collect(Collectors.joining("|"));
  }

  private List<Quote> extractQuotes(List<Share> shares, QuickQuoteResult quickQuoteResult) {
    return quickQuoteResult.getQuotes().stream().map(quickQuote -> createQuote(quickQuote, shares))
        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
  }

  private QuickQuoteResult retrieveQuotes(String keys) {
    log.debug("Retrieve quotes for {}", keys);
    var response = quickQuoteService.retrieve(keys.toUpperCase(), "json");
    var quickQuoteResponse = response.readEntity(QuickQuoteResponse.class);
    log.debug("Received: {}", quickQuoteResponse);
    return quickQuoteResponse.getQuickQuoteResult();
  }

  private Optional<Quote> createQuote(QuickQuote quickQuote, List<Share> shares) {
    if (quickQuote.getLast() == null || quickQuote.getName() == null || quickQuote.getSymbol() == null) {
      return Optional.empty();
    }
    
    for (var share : shares) {
      var key = quickQuote.getSymbol().toUpperCase();
      if (share.getKey().equalsIgnoreCase(key)) {
        return Optional.of(convertTo(quickQuote, share));
      }
    }
    return Optional.empty();
  }

  private Quote convertTo(QuickQuote quickQuote, Share share) {
    share.setKey(quickQuote.getSymbol().toUpperCase());
    share.setName(quickQuote.getName());
    
    var quote = new Quote(share);
    quote.setPrice(quickQuote.getLast());
    quote.setCurrency(StringUtils.defaultString(quickQuote.getCurrencyCode(), DEFAULT_CURRENCY));
    quote.setFrom(quickQuote.getLastTime());
    return quote;
  }

  private Optional<Quote> createQuote(QuickQuoteResult quickQuoteResult, Share share) {
    var quotes = quickQuoteResult.getQuotes();
    return quotes.isEmpty() ? Optional.empty() : createQuote(quotes.get(0), Collections.singletonList(share));
  }
}
