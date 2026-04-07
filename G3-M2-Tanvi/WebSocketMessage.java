package com.trading.platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class WebSocketMessage<T> {
    private String topic;      // ORDERS, TRADES, MARKET_DATA, OPTION_PRICES
    private String eventType;  // CREATED, UPDATED, CANCELLED, EXECUTED, PRICE_UPDATE
    private T payload;
    private LocalDateTime timestamp;

    public static <T> WebSocketMessage<T> of(String topic, String eventType, T payload) {
        return WebSocketMessage.<T>builder()
                .topic(topic).eventType(eventType)
                .payload(payload).timestamp(LocalDateTime.now())
                .build();
    }
}
