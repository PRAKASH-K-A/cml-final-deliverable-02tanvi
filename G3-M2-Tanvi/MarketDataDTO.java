package com.trading.platform.dto;

import com.trading.platform.entity.MarketData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class MarketDataDTO {
    private Long id;
    private String symbol;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private BigDecimal lastPrice;
    private BigDecimal closePrice;
    private BigDecimal openPrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private BigDecimal volume;
    private BigDecimal changePercent;
    private LocalDateTime timestamp;

    public static MarketDataDTO fromEntity(MarketData m) {
        return MarketDataDTO.builder()
                .id(m.getId()).symbol(m.getSymbol())
                .bidPrice(m.getBidPrice()).askPrice(m.getAskPrice())
                .lastPrice(m.getLastPrice()).closePrice(m.getClosePrice())
                .openPrice(m.getOpenPrice()).highPrice(m.getHighPrice())
                .lowPrice(m.getLowPrice()).volume(m.getVolume())
                .changePercent(m.getChangePercent()).timestamp(m.getTimestamp())
                .build();
    }
}
