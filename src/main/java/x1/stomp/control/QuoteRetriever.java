package x1.stomp.control;

import java.util.*;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import io.micrometer.core.annotation.Timed;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.Logged;
import x1.stomp.util.Metered;

@ApplicationScoped
@Logged(onlyFailures = false)
@Metered
public class QuoteRetriever {
  private static final String DEFAULT_CURRENCY = "EUR";

  @Inject
  @RestClient
  private QuickQuoteService quickQuoteService;

  @Inject
  @ConfigProperty(name = "x1.stomp.control.QuickQuoteService/mp-rest/format", defaultValue = "json")
  private String format;

  @Timed
  public Optional<Quote> retrieveQuote(Share share) {
    return createQuote(retrieveQuotes(share.getKey()), share);
  }

  @Timed
  public List<Quote> retrieveQuotes(List<Share> shares) {
    return (shares.isEmpty()) ? Collections.emptyList() : extractQuotes(shares, retrieveQuotes(joinKeys(shares)));
  }

  private String joinKeys(List<Share> shares) {
    return shares.stream().map(Share::getKey).collect(Collectors.joining("|"));
  }

  private List<Quote> extractQuotes(List<Share> shares, QuickQuoteResult quickQuoteResult) {
    return quickQuoteResult.getQuotes().stream().map(quickQuote -> createQuote(quickQuote, shares))
        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
  }

  private QuickQuoteResult retrieveQuotes(String keys) {
    try (var response = quickQuoteService.retrieve(keys.toUpperCase(), format)) {
      var status = Status.fromStatusCode(response.getStatus());
      if (status != Status.OK) {
        var body = response.readEntity(String.class);
        throw new WebApplicationException(body, status);
      }
      return switch (format) {
        case "xml" -> response.readEntity(QuickQuoteResult.class);
        case "json" -> response.readEntity(QuickQuoteResponse.class).getQuickQuoteResult();
        default -> throw new IllegalArgumentException(format);
      };
    }
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
    quote.setCurrency(Objects.toString(quickQuote.getCurrencyCode(), DEFAULT_CURRENCY));
    quote.setFrom(quickQuote.getLastTime());
    return quote;
  }

  private Optional<Quote> createQuote(QuickQuoteResult quickQuoteResult, Share share) {
    var quotes = quickQuoteResult.getQuotes();
    return quotes.isEmpty() ? Optional.empty() : createQuote(quotes.getFirst(), Collections.singletonList(share));
  }
}
