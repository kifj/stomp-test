package x1.stomp.websockets;

import io.opentelemetry.api.trace.Tracer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.slf4j.Logger;
import x1.service.registry.Service;
import x1.service.registry.Services;
import x1.stomp.control.QuoteRetriever;
import x1.stomp.control.QuoteUpdater;
import x1.stomp.control.ShareSubscription;
import x1.stomp.model.Command;
import x1.stomp.model.Quote;
import x1.stomp.model.Share;
import x1.stomp.util.JsonHelper;
import x1.stomp.version.VersionData;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static x1.service.registry.Protocol.WS;
import static x1.service.registry.Protocol.WSS;
import static x1.service.registry.Technology.WEBSOCKETS;

@ServerEndpoint("/ws/stocks")
@Services(services = {@Service(technology = WEBSOCKETS, value = "/ws/stocks", version = VersionData.APP_VERSION_MAJOR_MINOR, protocols = {WS, WSS})})
@ApplicationScoped
public class ShareSubscriptionWebSocketServerEndpoint {

    @Inject
    private ShareSubscription shareSubscription;

    @Inject
    private QuoteRetriever quoteRetriever;

    @Inject
    private QuoteUpdater quoteUpdater;

    @Inject
    private Logger log;

    @Inject
    private JsonHelper jsonHelper;

    @Inject
    private SessionHolder sessionHolder;

    @Inject
    private Tracer tracer;

    @OnOpen
    public void onConnectionOpen(Session session) {
        log.debug("Connection opened for session {}", session.getId());
        sessionHolder.put(session.getId(), session);
        quoteUpdater.updateQuotes();
    }

    @OnMessage
    public String onMessage(String message, Session session) throws IOException {
        log.debug("Received message {} for session {}", message, session.getId());
        var command = jsonHelper.fromJSON(message, Command.class);
        if (command.getAction() == null || StringUtils.isEmpty(command.getKey())) {
            log.warn("Incomplete command: {}", command);
            return null;
        }
        String result = null;
        switch (command.getAction()) {
            case SUBSCRIBE -> result = jsonHelper.toJSON(subscribe(command.getKey()).orElse(null));
            case UNSUBSCRIBE -> unsubscribe(command.getKey());
            default -> log.warn("Unknown command: {}", message);
        }
        return result;
    }

    private void unsubscribe(String key) {
        log.info("Unsubscribe: {}", key);
        var span = tracer.spanBuilder("/ws/stocks").setAttribute("command", "unsubscribe").setAttribute("key", key).startSpan();
        try {
            shareSubscription.find(key).ifPresent(shareSubscription::unsubscribe);
        } finally {
            span.end();
        }
    }

    private Optional<Quote> subscribe(String key) {
        log.info("Subscribe: {}", key);
        var span = tracer.spanBuilder("/ws/stocks").setAttribute("command", "subscribe").setAttribute("key", key).startSpan();
        try {
            var share = new Share(key);
            var quote = quoteRetriever.retrieveQuote(share);
            quote.ifPresent(q -> shareSubscription.subscribe(q.getShare()));
            return quote;
        } finally {
            span.end();
        }
    }

    @OnClose
    public void onConnectionClose(Session session) {
        log.debug("Connection close for session {}", session.getId());
        sessionHolder.remove(session.getId());
    }

    @OnError
    public void error(Session session, Throwable t) {
        log.warn("Connection error for session {} with error {}", session.getId(), t.getMessage());
        sessionHolder.remove(session.getId());
    }

    @Incoming("from-kafka")
    public CompletionStage<Void> receive(Message<String> message) {
        try {
            var payload = message.getPayload();
            log.debug("Received quote for {}", payload);
            sessionHolder.values().forEach(session -> sendMessage(payload, session));
            return message.ack();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return message.nack(e);
        }
    }

    @OnMessage
    public void onMessage(PongMessage message, Session session) {
        String answer = null;
        if (message.getApplicationData().hasArray()) {
            answer = new String(message.getApplicationData().array(), StandardCharsets.UTF_8);
        }
        log.debug("Received pong [{}] for session {}", answer, session.getId());
    }

    private void sendMessage(String payload, Session session) {
        try {
            session.getBasicRemote().sendText(payload);
        } catch (ClosedChannelException e) {
            sessionHolder.remove(session.getId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
