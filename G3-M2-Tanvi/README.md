# G3-M2: Market Data Store + Subscription API

## Module Overview

This module is part of **Group 3** of the Trading Platform project. It is responsible for maintaining an in-memory store of the latest market prices and providing a real-time WebSocket subscription service for clients to receive live price updates.


## Responsibilities

- **In-memory latest-price store** keyed by symbol for fast O(1) retrieval
- **WebSocket subscription service** allowing clients to subscribe/unsubscribe by symbol
- **Snapshot + incremental update protocol** — clients receive a full snapshot on connect, followed by delta updates only

---

## Files Submitted

### `service/MarketDataService.java`
The core service that maintains an in-memory map of the latest prices keyed by symbol. Receives `MarketDataUpdated` events from G3-M1 (Market Data File Poller) and updates the store. Exposes O(1) price lookup by symbol.

### `service/WebSocketBroadcastService.java`
Handles broadcasting of price updates to all subscribed WebSocket clients. On initial subscription, sends a full snapshot of current prices. Subsequently sends only incremental (delta) updates to reduce unnecessary data transfer.

### `controller/MarketDataController.java`
REST API controller exposing endpoints for clients to query the latest market price by symbol. Acts as the HTTP interface to the in-memory price store.

### `config/WebSocketConfig.java`
Configures the WebSocket message broker and STOMP endpoints. Sets up the subscribe/unsubscribe infrastructure that clients use to register interest in specific symbols.

### `dto/MarketDataDTO.java`
Data Transfer Object representing a market price entry (symbol, bid, offer, last, close). Used for both snapshot and incremental update payloads.

### `dto/WebSocketMessage.java`
Defines the structure of messages sent over the WebSocket stream, including message type (SNAPSHOT or UPDATE), symbol, and price data.

### `dto/ApiResponse.java`
Generic API response wrapper used by the REST controller to standardise HTTP responses.

---

## How It Fits Into the System

```
G3-M1 (File Poller)
    │
    │  MarketDataUpdated events
    ▼
G3-M2 (Market Data Store + Subscription API)  ◄──── Client subscribe/unsubscribe
    │
    │  Snapshot on connect + incremental updates via WebSocket
    ▼
Connected Clients (UI / G3-M4 Trade-driven Pricing)
```

- Consumes events from **G3-M1** (Market Data File Poller)
- Feeds real-time prices to **G3-M4** (Trade-driven Pricing Updates) and UI clients
- Provides reference price data used by **G3-M3** (Options Pricing Engine)


## Key Design Decisions

- **ConcurrentHashMap** used for the in-memory store to ensure thread-safe O(1) reads and writes
- **STOMP over WebSocket** used for the subscription protocol, enabling topic-based pub/sub by symbol
- **Snapshot-first protocol**: new subscribers always receive the full current state before incremental updates begin, preventing stale UI on connect
- **Throttling awareness**: designed to work with G3-M4's throttle (100ms per symbol) to avoid UI churn

