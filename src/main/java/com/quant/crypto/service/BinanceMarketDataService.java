package com.quant.crypto.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.crypto.util.ConfigManager;
import com.quant.crypto.model.Trade;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Service class responsible for interacting with the Binance REST API.
 * Handles HTTP requests, JSON parsing, and error logging using Log4j2.
 */
public class BinanceService {

    private static final Logger logger = LogManager.getLogger(BinanceService.class);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public BinanceService() {
        // HTTP Client'a timeout eklemek iyi bir pratiktir (10 sn)
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Fetches the specific ticker price for a symbol.
     * This is faster and lighter than fetching trade lists.
     * Endpoint: /api/v3/ticker/price
     *
     * @param symbol The trading pair (e.g., BTCUSDT)
     * @return The current price as a String (e.g., "95000.50"), or "0.0" if failed.
     */
    public String getPrice(String symbol) {

        String baseUrl = ConfigManager.getProperty("api.base.url", "https://api.binance.com");
        String endpoint = "/api/v3/ticker/price";

        // URL Construct: https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT
        String fullUrl = baseUrl + endpoint + "?symbol=" + symbol.toUpperCase();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET()
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long duration = System.currentTimeMillis() - start;

            if (response.statusCode() == 200) {
                // Gelen JSON şöyledir: {"symbol":"BTCUSDT", "price":"95123.45"}
                // Bunu tüm Trade objesine çevirmek yerine sadece "price" alanını okuyoruz (Daha hızlı)
                JsonNode root = objectMapper.readTree(response.body());
                String price = root.get("price").asText();

                // REST Latency'yi görmek için debug log
                logger.debug("🐢 REST Poll Latency: {} ms | Price: {}", duration, price);

                return price;
            } else {
                logger.error("⛔ REST API Error: {}", response.statusCode());
                return "0.0";
            }

        } catch (Exception e) {
            logger.error("❌ Failed to fetch price via REST", e);
            return "0.0";
        }
    }

    public String getStatus(String symbol) {

        String baseUrl = ConfigManager.getProperty("api.testnet.base.url", "https://testnet.binance.vision");

        // URL Construct: https://api.binance.com/api/v3/ticker/price?symbol=BTCUSDT
        String fullUrl = baseUrl + "?symbol=" + symbol.toUpperCase();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET()
                    .build();

            long start = System.currentTimeMillis();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            long duration = System.currentTimeMillis() - start;

            if (response.statusCode() == 200) {
                // Gelen JSON şöyledir: {"symbol":"BTCUSDT", "price":"95123.45"}
                // Bunu tüm Trade objesine çevirmek yerine sadece "price" alanını okuyoruz (Daha hızlı)
                JsonNode root = objectMapper.readTree(response.body());
                String price = root.get("price").asText();

                // REST Latency'yi görmek için debug log
                logger.debug("🐢 REST Poll Latency: {} ms | Price: {}", duration, price);

                return price;
            } else {
                logger.error("⛔ REST API Error: {}", response.statusCode());
                return "0.0";
            }

        } catch (Exception e) {
            logger.error("❌ Failed to fetch price via REST", e);
            return "0.0";
        }
    }

    /**
     * Fetches the most recent trades for a given symbol from the exchange.
     * Uses configuration settings for endpoints and limits.
     *
     * @param symbol The trading pair symbol (e.g., "BTCUSDT").
     * @return A list of {@link Trade} objects, or an empty list if the API call fails.
     */
    public List<Trade> getRecentTrades(String symbol) {
        // Retrieve settings from the configuration manager
        String baseUrl = ConfigManager.getProperty("api.base.url", "https://api.binance.com");
        String endpoint = "/api/v3/trades"; // Eğer configde yoksa default bu
        String limit = "10"; // Default limit

        // Construct the full API URL
        String fullUrl = baseUrl + endpoint + "?limit=" + limit + "&symbol=" + symbol.toUpperCase();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .GET()
                    .build();

            logger.debug("📡 Sending Request to: {}", fullUrl);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                logger.error("⛔ API Error! Status Code: {} | Response: {}", response.statusCode(), response.body());
                return Collections.emptyList();
            }

            // Deserialize JSON response to Trade objects
            List<Trade> trades = objectMapper.readValue(response.body(), new TypeReference<List<Trade>>() {});

            // Enrich the data: Set pair manually since API doesn't provide it inside the list
            trades.forEach(t -> t.setPair(symbol));

            return trades;

        } catch (Exception e) {
            logger.error("❌ Failed to fetch trades from Binance API", e);
            return Collections.emptyList();
        }
    }
}