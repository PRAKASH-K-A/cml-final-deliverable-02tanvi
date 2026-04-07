package com.trading.platform.service;

import com.trading.platform.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastOrder(String eventType, OrderDTO order) {
        WebSocketMessage<OrderDTO> msg = WebSocketMessage.of("ORDERS", eventType, order);
        messagingTemplate.convertAndSend("/topic/orders", msg);
        messagingTemplate.convertAndSend("/topic/orders/" + order.getSymbol(), msg);
        log.debug("Broadcast order event: {} for {}", eventType, order.getOrderRefNumber());
    }

    public void broadcastTrade(String eventType, TradeDTO trade) {
        WebSocketMessage<TradeDTO> msg = WebSocketMessage.of("TRADES", eventType, trade);
        messagingTemplate.convertAndSend("/topic/trades", msg);
        messagingTemplate.convertAndSend("/topic/trades/" + trade.getSymbol(), msg);
        log.debug("Broadcast trade event: {} for {}", eventType, trade.getTradeRefNumber());
    }

    public void broadcastMarketData(String eventType, MarketDataDTO md) {
        WebSocketMessage<MarketDataDTO> msg = WebSocketMessage.of("MARKET_DATA", eventType, md);
        messagingTemplate.convertAndSend("/topic/market-data", msg);
        messagingTemplate.convertAndSend("/topic/market-data/" + md.getSymbol(), msg);
    }

    public void broadcastOptionPrice(String eventType, OptionPriceDTO op) {
        WebSocketMessage<OptionPriceDTO> msg = WebSocketMessage.of("OPTION_PRICES", eventType, op);
        messagingTemplate.convertAndSend("/topic/option-prices", msg);
        messagingTemplate.convertAndSend("/topic/option-prices/" + op.getSymbol(), msg);
    }
}
