package x1.stomp.service;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;

import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;

public class QuoteRetriever {
  private static final String URL = "https://quote.cnbc.com/quote-html-webservice/quote.htm?symbols={0}&output=json";
  private static final String DEFAULT_CURRENCY = "EUR";

  @Inject
  private Logger log;

  public Quote retrieveQuote(Share share) {
    try {
      String content = retrieveInternal(share.getKey());
      return createQuote(content, share);
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
        buffer.append('|');
      }
      buffer.append(share.getKey());
    }
    try {
      String content = retrieveInternal(buffer.toString());
      return extractQuotes(shares, content);
    } catch (IOException e) {
      log.warn("Cound not retrieve quotes for " + buffer, e);
      return new ArrayList<>();
    }
  }

  private List<Quote> extractQuotes(List<Share> shares, String content) throws IOException {
    List<Quote> result = new ArrayList<>();
    QuickQuoteResult quickQuoteResult = JsonHelper.fromJSON(content, QuickQuoteResult.class);
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

  private String retrieveInternal(String keys) throws IOException {
    log.debug("Retrieve quotes for " + keys);
    String targetUrl = MessageFormat.format(URL, keys.toUpperCase());
    String content = Request.Get(targetUrl).execute().returnContent().asString();
    log.debug("Received content size:" + content.length());
    return content;
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

  private Quote createQuote(String content, Share share) throws IOException {
    QuickQuoteResult quickQuoteResult = JsonHelper.fromJSON(content, QuickQuoteResult.class);
    if (quickQuoteResult.getQuotes() != null && quickQuoteResult.getQuotes().isEmpty()) {
      return null;
    }
    return createQuote(quickQuoteResult.getQuotes().get(0), Arrays.asList(share));
  }
}
