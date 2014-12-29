package x1.stomp.test;

import static org.junit.Assert.*;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import x1.stomp.model.Share;
import x1.stomp.service.QuoteUpdater;
import x1.stomp.service.ShareSubscription;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

@RunWith(Arquillian.class)
public class ShareSubscriptionTest {
	@Deployment
	public static Archive<?> createTestArchive() {
		File[] libraries = Maven
				.resolver()
				.loadPomFromFile("pom.xml")
				.resolve("org.apache.httpcomponents:fluent-hc", "org.apache.commons:commons-lang3",
						"com.wordnik:swagger-jaxrs_2.10").withTransitivity().asFile();
		return ShrinkWrap.create(WebArchive.class, "stomp-test.war").addPackages(true, "x1.stomp")
				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsWebInfResource("test-ds.xml")
				.addAsWebInfResource("jboss-deployment-structure.xml")
				.addAsLibraries(libraries);
	}

	@Inject
	private ShareSubscription shareSubscription;

	@Inject
	private QuoteUpdater quoteUpdater;

	@Inject
	private Logger log;

	@Test
	public void testSubscribe() throws Exception {
    Share share = new Share();
    share.setKey("MSFT");
    share.setName("Microsoft Corpora");
    
		while (true) {
			Share existing = shareSubscription.find(share.getKey());
			if (existing != null) {
				shareSubscription.unsubscribe(existing);
			} else {
				break;
			}
		}
	  
		shareSubscription.subscribe(share);
		assertNotNull(share.getId());
    assertNotNull(share.getVersion());
		log.info("{} was persisted with id {}", share.getName(), share.getId());
		//nothing happens 
    shareSubscription.subscribe(share);

		share = shareSubscription.find(share.getKey());
		assertNotNull(share);
		assertEquals(1, shareSubscription.list().size());

		shareSubscription.unsubscribe(share);
		assertNull(shareSubscription.find(share.getKey()));
	}

	@Test
	public void testQuoteUpdater() throws Exception {
		Share share = new Share();
		share.setKey("GOOG");
		share.setName("Google");
		shareSubscription.subscribe(share);
		quoteUpdater.updateQuotes();
		assertEquals(1, quoteUpdater.getLastUpdateCount());
		Thread.sleep(3000);
    share = shareSubscription.find(share.getKey());
		shareSubscription.unsubscribe(share);
	}
}
