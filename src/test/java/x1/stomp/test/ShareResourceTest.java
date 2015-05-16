package x1.stomp.test;


import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import static org.junit.Assert.*;

import javax.inject.Inject;

import x1.stomp.model.Share;
import x1.stomp.rest.ErrorResponse;

@RunWith(Arquillian.class)
public class ShareResourceTest {
	private String baseUrl;

	@Inject
	private Logger log;

	@Deployment
	public static Archive<?> createTestArchive() {
		File[] libraries = Maven
				.resolver()
				.loadPomFromFile("pom.xml")
				.resolve(
				    "org.apache.httpcomponents:fluent-hc", 
				    "org.apache.commons:commons-lang3",
						"com.wordnik:swagger-jaxrs_2.10")
				.withTransitivity()
				.asFile();

		return ShrinkWrap.create(WebArchive.class, "stomp-test.war")
		    .addPackages(true, "x1.stomp")
				.addAsResource("META-INF/test-persistence.xml", "META-INF/persistence.xml")
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
				.addAsWebInfResource("test-ds.xml")
				.addAsWebInfResource("jboss-deployment-structure.xml")
				.addAsLibraries(libraries);
	}
	
  @Before
  public void setup() {
    baseUrl = "http://localhost:8080/stomp-test/rest";
  }

	@Test
	public void testFindShareNotFound() throws Exception {
		log.debug("begin testFindShareNotFound");
		Client client = ClientBuilder.newClient();
    Response response = client.target(baseUrl + "/shares/{key}").resolveTemplate("key", "AAPL")
        .request(MediaType.APPLICATION_JSON).get();
	assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
		log.debug("end testFindShareNotFound");
	}

	@Test
	public void testAddAndFindShare() throws Exception {
		log.debug("begin testAddAndFindShare");
		Share share = new Share();
		share.setKey("BMW.DE");
		share.setName("Bayerische Motoren Werke AG");
		
		Client client = ClientBuilder.newClient();
    Share created = client.target(baseUrl + "/shares/").request()
        .header("Correlation-Id", UUID.randomUUID().toString())
        .post(Entity.entity(share, MediaType.APPLICATION_JSON), Share.class);
		assertNotNull(created);
		assertNull(created.getId());
		assertEquals("BMW.DE", share.getKey());
		Thread.sleep(10000);
    Share found = client.target(baseUrl + "/shares/{key}").resolveTemplate("key", "BMW.DE")
        .request(MediaType.APPLICATION_JSON).get(Share.class);
		assertNotNull(found);
		assertNull(created.getId());
		assertEquals("BMW.DE", share.getKey());
		
    List<Share> shares = client.target(baseUrl + "/shares").request(MediaType.APPLICATION_JSON)
        .get(new GenericType<List<Share>>() {
        });
		assertEquals(1, shares.size());
		
    Response response3 = client.target(baseUrl + "/shares/{key}").resolveTemplate("key", share.getKey())
        .request(MediaType.APPLICATION_JSON).delete();
		assertEquals(Status.OK.getStatusCode(), response3.getStatus());
		
		log.debug("end testAddAndFindShare");
	}

	@Test
	public void testAddShareInvalid() throws Exception {
		log.debug("begin testAddShareInvalid");
		Share share = new Share();
		share.setKey("GOOG");
		Client client = ClientBuilder.newClient();
    Response response = client.target(baseUrl + "/shares").request(MediaType.APPLICATION_JSON)
        .post(Entity.entity(share, MediaType.APPLICATION_XML));
		assertEquals(Status.PRECONDITION_FAILED.getStatusCode(), response.getStatus());
		ErrorResponse errorResponse = response.readEntity(ErrorResponse.class);
		assertNotNull(errorResponse);
		assertEquals(2, errorResponse.getErrors().size());

		Response response2 = client.target(baseUrl + "/shares/{key}").resolveTemplate("key", "GOOG")
        .request(MediaType.APPLICATION_JSON).get();
		assertEquals(Status.NOT_FOUND.getStatusCode(), response2.getStatus());
		log.debug("end testAddShareInvalid");
	}
}
