package com.trading.platform.service;

import com.trading.platform.dto.MarketDataDTO;
import com.trading.platform.entity.MarketData;
import com.trading.platform.repository.MarketDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketDataService {

    private final MarketDataRepository marketDataRepository;
    private final WebSocketBroadcastService broadcastService;

    // In-memory latest prices for O(1) access
    private final Map<String, MarketDataDTO> latestPriceCache = new ConcurrentHashMap<>();

    private static final List<String> SYMBOLS = List.of(
        "AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "META", "NVDA", "JPM", "GS", "IBM"
    );

    private final Random random = new Random();

    @Transactional
    @Scheduled(fixedDelay = 3000)  // Simulate market data polling every 3s
    public void pollMarketData() {
        for (String symbol : SYMBOLS) {
            MarketDataDTO existing = latestPriceCache.get(symbol);
            double base = existing != null ? existing.getLastPrice().doubleValue() : getBasePrice(symbol);
            double change = base * (0.995 + random.nextDouble() * 0.01); // ±0.5% move
            BigDecimal last = bd(change);
            BigDecimal bid = bd(change * 0.999);
            BigDecimal ask = bd(change * 1.001);
            BigDecimal open = existing != null ? existing.getOpenPrice() : bd(base);
            BigDecimal close = existing != null ? existing.getClosePrice() : bd(base);
            BigDecimal high = existing != null ? existing.getHighPrice().max(last) : last;
            BigDecimal low  = existing != null ? existing.getLowPrice().min(last) : last;
            BigDecimal vol  = bd(random.nextInt(1000000) + 100000);
            BigDecimal pct  = close.compareTo(BigDecimal.ZERO) != 0
                    ? last.subtract(close).divide(close, 4, RoundingMode.HALF_UP).multiply(bd(100))
                    : BigDecimal.ZERO;

            MarketData md = MarketData.builder()
                    .symbol(symbol).lastPrice(last).bidPrice(bid).askPrice(ask)
                    .openPrice(open).closePrice(close).highPrice(high).lowPrice(low)
                    .volume(vol).changePercent(pct).timestamp(LocalDateTime.now())
                    .build();
            marketDataRepository.save(md);

            MarketDataDTO dto = MarketDataDTO.fromEntity(md);
            latestPriceCache.put(symbol, dto);
            broadcastService.broadcastMarketData("PRICE_UPDATE", dto);
        }
    }

    public MarketDataDTO getLatestBySymbol(String symbol) {
        if (latestPriceCache.containsKey(symbol)) {
            return latestPriceCache.get(symbol);
        }
        return marketDataRepository.findTopBySymbolOrderByTimestampDesc(symbol)
                .map(MarketDataDTO::fromEntity)
                .orElseThrow(() -> new RuntimeException("No market data for: " + symbol));
    }

    public List<MarketDataDTO> getAllLatest() {
        if (!latestPriceCache.isEmpty()) {
            return new ArrayList<>(latestPriceCache.values());
        }
        return SYMBOLS.stream()
                .map(s -> marketDataRepository.findTopBySymbolOrderByTimestampDesc(s))
                .filter(Optional::isPresent).map(o -> MarketDataDTO.fromEntity(o.get()))
                .collect(Collectors.toList());
    }

    public List<MarketDataDTO> getHistoryBySymbol(String symbol, int limit) {
        return marketDataRepository.findBySymbolOrderByTimestampDesc(symbol, PageRequest.of(0, limit))
                .stream().map(MarketDataDTO::fromEntity).collect(Collectors.toList());
    }

    private double getBasePrice(String symbol) {
        Map<String, Double> bases = Map.of(
            "AAPL", 185.0, "GOOGL", 175.0, "MSFT", 420.0, "AMZN", 190.0, "TSLA", 245.0,
            "META", 500.0, "NVDA", 875.0, "JPM", 195.0, "GS", 420.0, "IBM", 165.0
        );
        return bases.getOrDefault(symbol, 100.0);
    }

    private BigDecimal bd(double val) {
        return BigDecimal.valueOf(val).setScale(2, RoundingMode.HALF_UP);
    }
}
