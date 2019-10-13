package x1.stomp.test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import x1.stomp.control.QuoteUpdater;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Share;
import x1.stomp.version.VersionData;

import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class ShareSubscriptionTest {
  @Deployment
  public static Archive<?> createTestArchive() {
    var libraries = Maven.resolver().loadPomFromFile("pom.xml")
        .resolve("io.swagger.core.v3:swagger-jaxrs2", "org.assertj:assertj-core").withTransitivity().asFile();
    return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war").addPackages(true, "x1.stomp")
        .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
        .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsWebInfResource("test-ds.xml")
        .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
  }

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private QuoteUpdater quoteUpdater;

  @Inject
  private Logger log;

  @Test
  public void testSubscribe() {
    var share = new Share();
    share.setKey("MSFT");
    share.setName("Microsoft Corpora");

    while (true) {
      var existing = shareSubscription.find(share.getKey());
      if (existing.isPresent()) {
        shareSubscription.unsubscribe(existing.get());
      } else {
        break;
      }
    }

    shareSubscription.subscribe(share);
    assertThat(share.getId()).isNotNull();
    assertThat(share.getVersion()).isNotNull();
    log.info("{} was persisted with id {}", share.getName(), share.getId());
    // nothing happens
    shareSubscription.subscribe(share);

    var s = shareSubscription.find(share.getKey());
    assertThat(s).isPresent();
    assertThat(shareSubscription.list()).size().isEqualTo(1);
    share = s.get();
    shareSubscription.unsubscribe(share);
    assertThat(shareSubscription.find(share.getKey())).isNotPresent();
  }

  @Test
  public void testQuoteUpdater() throws Exception {
    var share = new Share();
    share.setKey("GOOG");
    share.setName("Google");
    shareSubscription.subscribe(share);
    quoteUpdater.updateQuotes();
    assertThat(quoteUpdater.getLastUpdateCount()).isEqualTo(1);
    Thread.sleep(3000);
    shareSubscription.find(share.getKey()).ifPresent(shareSubscription::unsubscribe);
  }
}
