package com.trading.platform.controller;

import com.trading.platform.dto.*;
import com.trading.platform.service.MarketDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/market-data")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataService marketDataService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MarketDataDTO>>> getAllLatest() {
        return ResponseEntity.ok(ApiResponse.ok(marketDataService.getAllLatest()));
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<ApiResponse<MarketDataDTO>> getLatestBySymbol(@PathVariable String symbol) {
        return ResponseEntity.ok(ApiResponse.ok(marketDataService.getLatestBySymbol(symbol.toUpperCase())));
    }

    @GetMapping("/{symbol}/history")
    public ResponseEntity<ApiResponse<List<MarketDataDTO>>> getHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "50") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                marketDataService.getHistoryBySymbol(symbol.toUpperCase(), limit)));
    }
}
