# Project Architecture

## Key Components

### REST API

`RestApplication` exposes the REST API under `/rest`.

- `ShareResource` manages share subscriptions under `/rest/shares`. It lists and finds subscriptions and directly persists or removes them through `ShareSubscription`.
- `QuoteResource` serves current quotes under `/rest/quotes`. It resolves subscriptions and retrieves one or more quotes from the external quote provider. Multi-quote requests run asynchronously and have a timeout.

### Subscription Service

`ShareSubscription` is the transactional service for subscription state. It persists `Share` entities with JPA, supports lookup and listing, and fires CDI events after subscribe or unsubscribe operations succeed.

### Quote Integration

`QuoteRetriever` calls CNBC's quote service through the MicroProfile REST client `QuickQuoteService`. The base URL is configured in `microprofile-config.properties` as `https://quote.cnbc.com/quote-html-webservice`; the client calls `GET /quote.htm` with the `symbols` and `output=json` query parameters.

The service returns a `QuickQuoteResult` containing quote records for the requested symbols. `QuoteRetriever` validates the symbol, name, and last price, then converts each record into an application `Quote` with price, currency, and timestamp data. CNBC's public quote pages show the corresponding market-data fields, including last price, volume, ranges, and related company information. CNBC states that its displayed data may be delayed and is subject to its [market-data terms](https://www.cnbc.com/market-data-terms-of-service/). The endpoint itself is a legacy service interface; no separate public API reference was found. The [CNBC quotes service](https://quote.cnbc.com/quote-html-webservice) and [public quotes pages](https://www.cnbc.com/quotes/) provide the available public context.

Timeouts, circuit breaking, and response error mapping protect this integration.

### Quote Publication

`QuoteUpdater` is a startup singleton with a scheduled update job. It retrieves current quotes for all persisted subscriptions and publishes each quote as JSON through the `to-kafka` channel to the Kafka `stocks` topic. It can also publish an individual quote.

### WebSocket Delivery

`ShareSubscriptionWebSocketServerEndpoint` exposes `/ws/stocks` and consumes the `from-kafka` channel. It accepts JSON subscribe and unsubscribe commands from WebSocket clients, performs those operations directly, sends Kafka quote messages to active sessions, and removes failed sessions.

`SessionHolder` maintains the active WebSocket sessions. `SubscriptionEventListener` forwards successful subscription changes to those sessions.

### Web Frontend

The frontend is a static HTML5 application under `src/main/webapp`. It has no frontend framework or package-managed build. It uses CSS for layout and styling, local jQuery for page initialization and UI updates, and a JavaScript client for WebSocket communication.

`index.html` uses `stockmarket-ws.js` to connect directly to `/ws/stocks` with the browser WebSocket API. It sends subscribe and unsubscribe commands, manages the connection, and displays incoming quotes in a table containing the share symbol, name, price, and currency. It also shows connection or operation status and includes a debug console for client messages.

### Domain and Transport Models

- `Share` is the persisted subscription entity. Its stock symbol is unique and its display name is stored with it.
- `Quote` represents a current price, currency, timestamp, and associated share.
- `Command` and `Action` represent subscribe and unsubscribe requests.
- `SubscriptionEvent` represents a completed subscription change sent to connected clients.
- Wrapper and link model classes provide JSON/XML response shapes and REST hypermedia links.

### Runtime Dependencies

- WildFly provides the Jakarta EE runtime, CDI, JAX-RS, WebSocket, EJB, transactions, scheduling, and MicroProfile Reactive Messaging with the Kafka connector.
- PostgreSQL stores subscriptions through the JPA persistence unit `stomp-test` and datasource `java:jboss/datasources/stocksDS`.
- Apache Kafka provides the `stocks` topic used for quote publication.
- The external quote provider supplies current market data through the `QuickQuoteService` REST client.

## Business Purpose

The application tracks requested stock symbols and distributes current market quotes through REST, WebSocket, and Kafka interfaces. WebSocket subscriptions are stored after an initial valid quote supplies the share's readable name; REST clients provide the share data directly.

### REST Subscription Flow

A client submits a share to `POST /rest/shares`. `ShareResource` asks `ShareSubscription` to persist it and returns a created response. `DELETE /rest/shares/{key}` removes an existing subscription directly.

### WebSocket Subscription Flow

A WebSocket client sends a JSON subscribe or unsubscribe command to `/ws/stocks`. The endpoint validates the command, retrieves an initial quote for a new subscription, and uses `ShareSubscription` for the requested operation.

### Quote Lookup Flow

A client requests one quote or a set of quotes through `/rest/quotes`. The resource reads the relevant persisted subscriptions, `QuoteRetriever` calls the external provider, and the resource returns the converted quotes with links to related resources.

### Scheduled Quote Broadcast

`QuoteUpdater` periodically loads all subscriptions and requests fresh data. Each valid quote is serialized and published to Kafka topic `stocks`. The WebSocket consumer receives updates without polling the REST API and broadcasts them to active sessions.

### WebSocket Client Flow

A WebSocket client connects to `/ws/stocks` and sends commands over the connection. Quote updates from Kafka are broadcast to active WebSocket sessions, while successful subscription changes are sent as subscription events.

## Interfaces and Destinations

| Interface | Address or destination | Purpose |
| --- | --- | --- |
| REST | `/rest/shares` | Manage share subscriptions |
| REST | `/rest/quotes` | Retrieve current quotes |
| WebSocket | `/ws/stocks` | Send commands and receive live events |
| Kafka topic | `stocks` | Quote publication and consumption |
