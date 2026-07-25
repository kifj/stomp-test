package x1.stomp.test;

import java.net.URL;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import x1.stomp.boundary.JacksonConfig;

@ExtendWith(ArquillianExtension.class)
@Tag("Arquillian")
public abstract class AbstractIT {

    protected Client client;

    @ArquillianResource
    protected URL url;


    @BeforeEach
    void setup() {
        client = ClientBuilder.newClient().register(JacksonConfig.class);
    }

    @AfterEach
    void tearDown() {
        client.close();
    }

    public Integer getPortOffset() {
        return Integer.valueOf(System.getProperty("jboss.socket.binding.port-offset", "0"));
    }

    public String getHost() {
        return System.getProperty("jboss.bind.address", "127.0.0.1");
    }

}
