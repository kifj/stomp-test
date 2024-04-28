package x1.stomp.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import x1.stomp.boundary.ErrorResponse;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;
import java.util.UUID;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static x1.stomp.test.ResponseAssert.assertThat;
import static x1.stomp.test.ErrorResponseAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@DisplayName("ShareResource Test")
public class ShareResourceTest extends AbstractIT {

  private static final String HEADER_CORRELATION_ID = "Correlation-Id";
  private static final String PATH_QUOTES = "quotes";
  private static final String PATH_SHARES = "shares";
  private static final String PATH_PARAM_KEY = "{key}";
  private static final String PARAM_KEY = "key";
  private static final String TEST_SHARE = "AAPL";
  private static final String TEST_SHARE_INVALID = "XXXX";

  private String baseUrl;

  @BeforeEach
  void setup() {
    super.setup();
    baseUrl = url.toString() + "rest";
  }

  @Test
  void testFindShareNotFound() {
    try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY)
        .resolveTemplate(PARAM_KEY, TEST_SHARE).request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  @Test
  void testAddAndFindShare() throws Exception {
    var share = new Share();
    var key = "MSFT";
    var name = "Microsoft Corporation";
    share.setKey(key);
    share.setName(name);

    try (var response = client.target(baseUrl).path(PATH_SHARES).request()
        .header(HEADER_CORRELATION_ID, UUID.randomUUID().toString()).post(Entity.entity(share, APPLICATION_JSON))) {
      assertThat(response).hasStatus(CREATED);
      assertThat(response.getLocation())
          .isEqualTo(UriBuilder.fromUri(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).build(share.getKey()));
    }

    var loop = 0;
    while (true) {
      try {
        var found = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
            .request(APPLICATION_JSON).get(Share.class);
        assertThat(found).isNotNull();
        assertThat(found.getId()).isNull();
        assertThat(found.getKey()).isEqualTo(key);
        assertThat(found.getLinks()).hasSize(3);
        found.getLinks().forEach(link -> {
          assertThat(link.getUri()).isNotNull();
          assertThat(link.getRel()).isNotNull();
        });
        break;
      } catch (NotFoundException e) {
        Thread.sleep(500);
        loop++;
        if (loop == 20) {
          fail(e.getMessage());
        }
      }
    }

    var shares = client.target(baseUrl).path(PATH_SHARES).request(APPLICATION_JSON).get(new Shares());
    assertThat(shares).size().isEqualTo(1);

    var quote = client.target(baseUrl).path(PATH_QUOTES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, share.getKey())
        .request(APPLICATION_JSON).get(Quote.class);
    assertThat(quote).isNotNull();
    assertThat(quote.getCurrency()).isNotNull();
    assertThat(quote.getPrice()).isNotNull();
    assertThat(quote.getShare().getKey()).isEqualTo(share.getKey());

    var quotes = client.target(baseUrl).path(PATH_QUOTES).queryParam(PARAM_KEY, share.getKey(), TEST_SHARE_INVALID)
        .request(APPLICATION_JSON).get(new Quotes());
    assertThat(quotes).size().isEqualTo(1);

    try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY)
        .resolveTemplate(PARAM_KEY, share.getKey()).request(APPLICATION_JSON).delete()) {
      assertThat(response).hasStatus(OK);
    }

    try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
        .request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  @Test
  void testAddShareInvalid() {
    var key = "GOOG";
    var share = new Share(key);

    try (var response = client.target(baseUrl).path(PATH_SHARES).request(APPLICATION_JSON)
        .post(Entity.entity(share, MediaType.APPLICATION_XML))) {
      assertThat(response).hasStatus(BAD_REQUEST);
      var errorResponse = response.readEntity(ErrorResponse.class);
      assertThat(errorResponse).isNotNull().containsErrors(2).hasRequestUri().hasType("Invalid data");
    }

    try (var response = client.target(baseUrl).path(PATH_SHARES).path(PATH_PARAM_KEY).resolveTemplate(PARAM_KEY, key)
        .request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  @Test
  void testGetQuoteNotFound() {
    try (var response = client.target(baseUrl).path(PATH_QUOTES).path(PATH_PARAM_KEY)
        .resolveTemplate(PARAM_KEY, TEST_SHARE).request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  @Test
  void testGetQuotesNotFound() {
    try (var response = client.target(baseUrl).path(PATH_QUOTES).queryParam(PARAM_KEY, TEST_SHARE, TEST_SHARE_INVALID)
        .request(APPLICATION_JSON).get()) {
      assertThat(response).hasStatus(NOT_FOUND);
    }
  }

  private static final class Shares extends GenericType<List<Share>> {
  }

  private static final class Quotes extends GenericType<List<Quote>> {
  }

}
