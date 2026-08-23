# Project Architecture

## Key Components

### REST API

`RestApplication` exposes the REST API under `/rest`.

- `ShareResource` manages share subscriptions under `/rest/shares`. It lists and finds subscriptions, removes them directly, and sends subscribe commands to the JMS queue.
- `QuoteResource` serves current quotes under `/rest/quotes`. It resolves subscriptions and retrieves one or more quotes from the external quote provider. Multi-quote requests run asynchronously and have a timeout.

### Subscription Service

`ShareSubscription` is the transactional service for subscription state. It persists `Share` entities with JPA, supports lookup and listing, and fires CDI events after subscribe or unsubscribe operations succeed.

### Quote Integration

`QuoteRetriever` calls CNBC's quote service through the MicroProfile REST client `QuickQuoteService`. The base URL is configured in `microprofile-config.properties` as `https://quote.cnbc.com/quote-html-webservice`; the client calls `GET /quote.htm` with the `symbols` and `output=json` query parameters.

The service returns a `QuickQuoteResult` containing quote records for the requested symbols. `QuoteRetriever` validates the symbol, name, and last price, then converts each record into an application `Quote` with price, currency, and timestamp data. CNBC's public quote pages show the corresponding market-data fields, including last price, volume, ranges, and related company information. CNBC states that its displayed data may be delayed and is subject to its [market-data terms](https://www.cnbc.com/market-data-terms-of-service/). The endpoint itself is a legacy service interface; no separate public API reference was found. The [CNBC quotes service](https://quote.cnbc.com/quote-html-webservice) and [public quotes pages](https://www.cnbc.com/quotes/) provide the available public context.

Timeouts, circuit breaking, and response error mapping protect this integration.

### Asynchronous Command Processing

`ShareMessageListener` consumes the `stocks` JMS queue. It processes subscribe and unsubscribe commands delivered either as Java object messages or as JSON byte messages. Subscribe commands retrieve an initial quote before the share is persisted.

The queue is also available through the broker's STOMP interface, allowing STOMP clients to submit the same commands.

### Quote Publication

`QuoteUpdater` is a startup singleton with a scheduled update job. It retrieves current quotes for all persisted subscriptions and publishes each quote as JSON to the `quotes` JMS topic. It can also publish an individual quote.

### WebSocket and STOMP Delivery

`ShareSubscriptionWebSocketServerEndpoint` exposes `/ws/stocks` and consumes the `quotes` JMS topic. It accepts JSON subscribe and unsubscribe commands from WebSocket clients, sends quote messages to active sessions, and removes failed sessions.

`SessionHolder` maintains the active WebSocket sessions. `SubscriptionEventListener` forwards successful subscription changes to those sessions. The same JMS queue and topic are exposed by ActiveMQ Artemis through STOMP destinations for broker-based clients.

### Web Frontend

The frontend is a static HTML5 application under `src/main/webapp`. It has no frontend framework or package-managed build. It uses CSS for layout and styling, local jQuery for page initialization and UI updates, and JavaScript clients for WebSocket and STOMP communication.

`index.html` uses `stockmarket-ws.js` to connect directly to `/ws/stocks` with the browser WebSocket API. `stompws.html` uses the bundled `stomp.js` client and connects to the broker's STOMP WebSocket endpoint. Both pages send subscribe and unsubscribe commands, manage the connection, and display incoming quotes in a table containing the share symbol, name, price, and currency. They also show connection or operation status and include a debug console for client messages.

### Domain and Transport Models

- `Share` is the persisted subscription entity. Its stock symbol is unique and its display name is stored with it.
- `Quote` represents a current price, currency, timestamp, and associated share.
- `Command` and `Action` represent subscribe and unsubscribe requests.
- `SubscriptionEvent` represents a completed subscription change sent to connected clients.
- Wrapper and link model classes provide JSON/XML response shapes and REST hypermedia links.

### Runtime Dependencies

- WildFly provides the Jakarta EE runtime, CDI, JAX-RS, WebSocket, EJB, JMS, transactions, and scheduling.
- PostgreSQL stores subscriptions through the JPA persistence unit `stomp-test` and datasource `java:jboss/datasources/stocksDS`.
- ActiveMQ Artemis provides the `stocks` command queue and `quotes` publication topic, including their STOMP endpoints.
- The external quote provider supplies current market data through the `QuickQuoteService` REST client.

## Business Purpose

The application tracks requested stock symbols and distributes current market quotes through synchronous and asynchronous interfaces. A subscription is stored only after the application has obtained an initial valid quote, which also supplies the share's readable name.

### REST Subscription Flow

A client submits a share to `POST /rest/shares`. The resource sends a subscribe command to the JMS queue and returns a created response. The message listener retrieves the initial quote and asks `ShareSubscription` to persist the share. `DELETE /rest/shares/{key}` removes an existing subscription directly.

### Asynchronous Subscription Flow

A REST or STOMP client can submit a subscribe or unsubscribe command to the stocks queue. `ShareMessageListener` validates the command, performs the requested operation, and uses the same subscription service as the REST boundary.

### Quote Lookup Flow

A client requests one quote or a set of quotes through `/rest/quotes`. The resource reads the relevant persisted subscriptions, `QuoteRetriever` calls the external provider, and the resource returns the converted quotes with links to related resources.

### Scheduled Quote Broadcast

`QuoteUpdater` periodically loads all subscriptions and requests fresh data. Each valid quote is serialized and published to the quotes topic. Topic consumers receive updates without polling the REST API.

### WebSocket/STOMP Client Flow

A WebSocket client connects to `/ws/stocks`, or a STOMP client connects through the broker. The client sends commands to the stocks queue and subscribes to the quotes topic. Quote updates are broadcast to active WebSocket sessions, while successful subscription changes are sent as subscription events.

## Interfaces and Destinations

| Interface | Address or destination | Purpose |
| --- | --- | --- |
| REST | `/rest/shares` | Manage share subscriptions |
| REST | `/rest/quotes` | Retrieve current quotes |
| WebSocket | `/ws/stocks` | Send commands and receive live events |
| JMS queue | `java:/jms/queue/stocks` | Internal subscription commands |
| JMS topic | `java:/jms/topic/quotes` | Internal quote broadcasts |
| STOMP queue | `jms.queue.stocksQueue` | Broker-facing subscription commands |
| STOMP topic | `jms.topic.quotesTopic` | Broker-facing quote broadcasts |
