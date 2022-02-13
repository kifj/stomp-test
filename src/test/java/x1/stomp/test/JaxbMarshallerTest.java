package x1.stomp.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;

import javax.xml.bind.JAXBContext;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import x1.stomp.control.QuickQuote;
import x1.stomp.control.QuickQuoteResult;
import x1.stomp.version.VersionData;

@ExtendWith(ArquillianExtension.class)
public class JaxbMarshallerTest {

  @Deployment
  public static Archive<?> createTestArchive() {
    var libraries = Maven.resolver().loadPomFromFile("pom.xml")
        .resolve("org.assertj:assertj-core", "org.hamcrest:hamcrest-library").withTransitivity().asFile();

    return ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war").addPackages(true, "x1.stomp")
        .addAsResource("test-persistence.xml", "META-INF/persistence.xml")
        .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
        .addAsResource("quickquoteresult.xml").addAsWebInfResource("beans.xml").addAsWebInfResource("test-ds.xml")
        .addAsWebInfResource("jboss-deployment-structure.xml").addAsLibraries(libraries);
  }

  @Test
  public void readQuickQuote() throws Exception {
    var ctx = JAXBContext.newInstance(QuickQuoteResult.class, QuickQuote.class);
    var unmarshaller = ctx.createUnmarshaller();
    var is = Objects.requireNonNull(getClass().getClassLoader().getResource("quickquoteresult.xml"));

    var result = (QuickQuoteResult) unmarshaller.unmarshal(is);
    assertThat(result).isNotNull();
    assertThat(result.getQuotes()).hasSize(1);
    var quote = result.getQuotes().get(0);
    assertThat(quote).isNotNull();
    assertThat(quote.getCountryCode()).isNotNull();
    assertThat(quote.getCurrencyCode()).isNotNull();
    assertThat(quote.getExchange()).isNotNull();
    assertThat(quote.getLast()).isNotNull();
    assertThat(quote.getLastTime()).isNotNull();
    assertThat(quote.getName()).isNotNull();
    assertThat(quote.getSymbol()).isNotNull();
    assertThat(quote.getVolume()).isNotNull();
  }

}
