package x1.arquillian;

import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import x1.service.test.ResolverTest;
import x1.stomp.version.VersionData;

@ArquillianSuiteDeployment
public class ArquillianSuite {
    @Deployment
    public static Archive<?> createTestArchive() {
        var libraries = Maven.resolver().loadPomFromFile("pom.xml")
                .resolve(
                        "x1.wildfly:service-registry",
                        "org.assertj:assertj-core",
                        "org.testcontainers:testcontainers",
                        "org.testcontainers:testcontainers-kafka",
                        "org.testcontainers:testcontainers-postgresql")
                .withTransitivity()
                .asFile();

        var archive = ShrinkWrap.create(WebArchive.class, VersionData.APP_NAME_MAJOR_MINOR + ".war")
                .addPackages(true, "x1.stomp")
                .addClass(ResolverTest.class)
                .addAsResource("microprofile-config.properties", "META-INF/microprofile-config.properties")
                .addAsResource("service-registry.properties")
                .addAsWebInfResource("beans.xml")
                .addAsWebInfResource("jboss-deployment-structure.xml")
                .addAsLibraries(libraries);

        if (Containers.isRemoteArquillian()) {
            return archive
                    .addAsResource("remote-persistence.xml", "META-INF/persistence.xml");
        } else {
            return archive
                    .addAsResource("managed-persistence.xml", "META-INF/persistence.xml")
                    .addAsWebInfResource("test-ds.xml");
        }
    }
}
