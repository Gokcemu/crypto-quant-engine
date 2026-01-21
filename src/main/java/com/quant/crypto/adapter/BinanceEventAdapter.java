package com.quant.crypto.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.crypto.model.event.OrderBookUpdateEvent;
import com.quant.crypto.model.event.PriceLevel;
import com.quant.crypto.model.event.PublicTradeEvent;
import com.quant.crypto.service.StrategyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class that converts raw JSON messages from Binance WebSocket
 * into Java domain events (PublicTradeEvent, OrderBookUpdateEvent).
 */
public class BinanceEventAdapter implements ExchangeEventAdapter{

    private static final Logger logger = LogManager.getLogger(BinanceEventAdapter.class);
    private final ObjectMapper objectMapper;
    private final StrategyService strategyService;

    public BinanceEventAdapter(StrategyService strategyService) {
        this.strategyService = strategyService;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Entry point for raw WebSocket messages.
     * @param jsonMessage The raw JSON string received from Binance.
     */
    public void processMessage(String jsonMessage) {
        try {
            // 1. JSON'ı ağaç yapısına (Node) çevir
            JsonNode rootNode = objectMapper.readTree(jsonMessage);

            // 2. Event tipini ("e" alanı) kontrol et
            if (!rootNode.has("e")) {
                return; // Event tipi yoksa işlem yapma (örn: heartbeat)
            }

            String eventType = rootNode.get("e").asText();

            // 3. Tipe göre doğru Java objesine çevir ve Stratejiye at
            switch (eventType) {
                case "aggTrade" -> {
                    PublicTradeEvent tradeEvent = mapToTradeEvent(rootNode);
                    strategyService.onEvent(tradeEvent);
                }
                case "depthUpdate" -> {
                    OrderBookUpdateEvent bookEvent = mapToBookEvent(rootNode);
                    strategyService.onEvent(bookEvent);
                }
                default -> logger.debug("Ignored raw event type: {}", eventType);
            }

        } catch (Exception e) {
            logger.error("Failed to parse message: {}", jsonMessage, e);
        }
    }

    // --- PRIVATE MAPPERS (JSON -> Java Conversion) ---

    private PublicTradeEvent mapToTradeEvent(JsonNode node) {
        // Binance Field Mapping:
        // "s": Symbol, "p": Price, "q": Quantity, "T": Timestamp, "m": isBuyerMaker
        return new PublicTradeEvent(
                node.get("s").asText(),
                node.get("p").asDouble(),
                node.get("q").asDouble(),
                node.get("T").asLong(),
                node.get("m").asBoolean()
        );
    }

    private OrderBookUpdateEvent mapToBookEvent(JsonNode node) {
        // Binance Field Mapping:
        // "s": Symbol, "U": FirstId, "u": FinalId, "E": Time
        // "b": Bids Array, "a": Asks Array

        List<PriceLevel> bids = mapPriceLevels(node.get("b"));
        List<PriceLevel> asks = mapPriceLevels(node.get("a"));

        return new OrderBookUpdateEvent(
                node.get("s").asText(),
                node.get("U").asLong(),
                node.get("u").asLong(),
                node.get("E").asLong(), // Event time genelde timestamp olarak kullanılır
                bids,
                asks
        );
    }

    private List<PriceLevel> mapPriceLevels(JsonNode listNode) {
        List<PriceLevel> levels = new ArrayList<>();
        if (listNode != null && listNode.isArray()) {
            for (JsonNode entry : listNode) {
                // Binance format: ["Price", "Qty"] -> String array gönderir!
                double price = Double.parseDouble(entry.get(0).asText());
                double qty = Double.parseDouble(entry.get(1).asText());
                levels.add(new PriceLevel(price, qty));
            }
        }
        return levels;
    }
}