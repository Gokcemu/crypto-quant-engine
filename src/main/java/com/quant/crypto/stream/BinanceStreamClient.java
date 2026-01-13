package com.quant.crypto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.crypto.model.TradeEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletionStage;

/**
 * Handles real-time WebSocket connections to the Binance Market Data Stream.
 * Acts as the "Subject" (Publisher) in the Observer Pattern, notifying subscribers
 * about price updates.
 */
public class BinanceStreamClient {

    private static final Logger logger = LogManager.getLogger(BinanceStreamClient.class);

    private String baseUrl;
    private final List<TradeEventListener> listeners = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Initializes the client by loading configuration properties.
     * @throws RuntimeException if configuration is missing.
     */
    public BinanceStreamClient() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) throw new RuntimeException("❌ Configuration file 'application.properties' not found!");

            Properties prop = new Properties();
            prop.load(input);
            this.baseUrl = prop.getProperty("api.websocket.base.url");

            if (this.baseUrl == null) throw new RuntimeException("❌ Property 'api.websocket.base.url' is missing!");
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load configuration", ex);
        }
    }

    /**
     * Subscribes a listener to receive trade events.
     * @param listener The observer implementing {@link TradeEventListener}.
     */
    public void subscribe(TradeEventListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Establishes a WebSocket connection for the specified symbol.
     * @param symbol The trading pair symbol (e.g., "BTCUSDT").
     */
    public void connect(String symbol) {
        String streamUrl = this.baseUrl + symbol.toLowerCase() + "@trade";
        logger.info("🔌 CONNECTING to WebSocket Stream: {}", streamUrl);

        HttpClient client = HttpClient.newHttpClient();

        client.newWebSocketBuilder()
                .buildAsync(URI.create(streamUrl), new WebSocketListener())
                .join();
    }

    /**
     * Inner class to handle WebSocket events.
     * Non-static to allow access to the outer 'listeners' list.
     */
    private class WebSocketListener implements WebSocket.Listener {

        private StringBuilder buffer = new StringBuilder();

        @Override
        public void onOpen(WebSocket webSocket) {
            logger.info("✅ WEBSOCKET OPENED! Real-time data stream started.");
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            // 1. Gelen parçayı tampona ekle
            buffer.append(data);

            // 2. Eğer bu mesajın SON parçasıysa (last == true), işlemi başlat
            if (last) {
                String fullMessage = buffer.toString();

                // Buffer'ı temizle ki sonraki mesaja hazır olsun
                buffer = new StringBuilder(); // veya buffer.setLength(0);

                try {
                    JsonNode node = mapper.readTree(fullMessage);
                    if (node.has("p") && node.has("E")) {
                        double price = node.get("p").asDouble();
                        long eventTime = node.get("E").asLong();

                        for (TradeEventListener listener : listeners) {
                            listener.onTradeEvent(price, eventTime);
                        }
                    }
                } catch (Exception e) {
                    logger.error("❌ Error parsing WS message: {}", fullMessage, e);
                }
            }

            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            logger.error("❌ WEBSOCKET ERROR: ", error);
        }
    }
}