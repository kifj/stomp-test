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
import x1.stomp.util.VersionData;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
public class ShareSubscriptionTest {
    @Deployment
    public static Archive<?> createTestArchive() {
        File[] libraries = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve("org.apache.commons:commons-lang3", "io.swagger:swagger-jaxrs", "org.assertj:assertj-core").withTransitivity().asFile();
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
        Share share = new Share();
        share.setKey("MSFT");
        share.setName("Microsoft Corpora");

        while (true) {
            Optional<Share> existing = shareSubscription.find(share.getKey());
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

        Optional<Share> s = shareSubscription.find(share.getKey());
        assertThat(s).isPresent();
        assertThat(shareSubscription.list()).size().isEqualTo(1);
        share = s.get();
        shareSubscription.unsubscribe(share);
        assertThat(shareSubscription.find(share.getKey())).isNotPresent();
    }

    @Test
    public void testQuoteUpdater() throws Exception {
        Share share = new Share();
        share.setKey("GOOG");
        share.setName("Google");
        shareSubscription.subscribe(share);
        quoteUpdater.updateQuotes();
        assertThat(quoteUpdater.getLastUpdateCount()).isEqualTo(1);
        Thread.sleep(3000);
        shareSubscription.find(share.getKey()).ifPresent(shareSubscription::unsubscribe);
    }
}
