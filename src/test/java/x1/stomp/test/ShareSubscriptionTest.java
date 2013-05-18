package x1.stomp.test;

import static org.junit.Assert.*;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;

import x1.stomp.model.Share;
import x1.stomp.service.ShareSubscription;
import x1.stomp.util.Resources;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

@RunWith(Arquillian.class)
public class ShareSubscriptionTest {
  @Deployment
  public static Archive<?> createTestArchive() {
    return ShrinkWrap.create(WebArchive.class, "stomp-test.war")
        .addClasses(Share.class, ShareSubscription.class, Resources.class)
        .addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
        .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml").addAsWebInfResource("test-ds.xml", "test-ds.xml");
  }

  @Inject
  private ShareSubscription shareSubscription;

  @Inject
  private Logger log;

  @Test
  public void testSubscribe() throws Exception {
    Share share = new Share();
    share.setKey("MSFT");
    share.setName("Microsoft Corpora");
    shareSubscription.subscribe(share);
    assertNotNull(share.getId());
    log.info(share.getName() + " was persisted with id " + share.getId());
  
    share = shareSubscription.find(share.getKey());
    assertNotNull(share);
    assertEquals(1, shareSubscription.list().size());
	  
    //shareSubscription.unsubscribe(share);	  
    //assertNull(shareSubscription.find(share.getKey()));
  }
}
