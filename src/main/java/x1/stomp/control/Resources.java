package x1.stomp.control;

import java.net.URL;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

public class Resources {
  @Inject
  @ConfigProperty(name = "x1.stomp.control.QuickQuoteService/mp-rest/url")
  private URL baseUrl;

  @Produces
  public QuickQuoteService createQuickQuoteService() {
    return RestClientBuilder.newBuilder().baseUrl(baseUrl).register(QuickQuoteResponseExceptionMapper.class)
        .build(QuickQuoteService.class);
  }
}
