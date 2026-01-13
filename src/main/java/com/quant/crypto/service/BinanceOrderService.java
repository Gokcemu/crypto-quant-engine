package com.quant.crypto.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quant.crypto.model.OrderRequest;
import com.quant.crypto.model.OrderResponse;
import com.quant.crypto.util.SignatureUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Order;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Properties;

/**
 * Service responsible for executing trade orders via the Binance REST API.
 * Handles HMAC-SHA256 signature generation and HTTP request construction.
 */
public class EnterOrderService {

    private static final Logger logger = LogManager.getLogger(EnterOrderService.class);

    private String apiKey;
    private String secretKey;
    private String baseUrl;

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public EnterOrderService() {
        this.httpClient = HttpClient.newHttpClient();
        this.mapper = new ObjectMapper();
        loadConfig();
    }

    /**
     * Loads the configuration properties (API keys, Base URLs) from the
     * 'application.properties' file located in the classpath (src/main/resources).
     *
     * @throws RuntimeException If the configuration file is not found or cannot be read.
     */
    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {

            if (input == null) {
                throw new RuntimeException("❌ Configuration file 'application.properties' not found in classpath!");
            }

            Properties prop = new Properties();
            prop.load(input);

            // Fetching values by their exact keys defined in the properties file
            this.apiKey = prop.getProperty("api.key");
            this.secretKey = prop.getProperty("api.secret");

            // Ensure this key matches exactly what is in your file (e.g., "api.base.url" or "api.testnet.base.url")
            this.baseUrl = prop.getProperty("api.testnet.base.url");

            // Validation check to prevent silent null pointers later
            if (this.apiKey == null || this.secretKey == null || this.baseUrl == null) {
                throw new RuntimeException("❌ Missing required properties in 'application.properties'. Check your keys.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    /**
     * Sends an order request to the Binance API.
     *
     * @param orderRequest The order details.
     * @return The response from the exchange mapped to {@link OrderResponse}.
     */
    public OrderResponse enterOrderRequest(OrderRequest orderRequest) {
        try {
            StringBuilder queryString = new StringBuilder();
            queryString.append("symbol=").append(orderRequest.getSymbol());
            queryString.append("&side=").append(orderRequest.getSide());
            queryString.append("&type=").append(orderRequest.getType());
            queryString.append("&quantity=").append(orderRequest.getQuantity());

            if (orderRequest.getPrice() != null) {
                queryString.append("&price=").append(orderRequest.getPrice());
            }
            if (orderRequest.getTimeInForce() != null) {
                queryString.append("&timeInForce=").append(orderRequest.getTimeInForce());
            }

            queryString.append("&timestamp=").append(System.currentTimeMillis());
            queryString.append("&recvWindow=5000");
            queryString.append("&newOrderRespType=FULL");

            String signature = SignatureUtil.getSignature(queryString.toString(), secretKey);
            queryString.append("&signature=").append(signature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v3/order?" + queryString))
                    .header("X-MBX-APIKEY", apiKey)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            logger.info("📤 Entering Order: {} {} {} @ {}",
                    orderRequest.getSide(), orderRequest.getQuantity(), orderRequest.getSymbol(), orderRequest.getType());

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            logger.info("Binance HTTP={} body={}", response.statusCode(), body);

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                OrderResponse ok = mapper.readValue(body, OrderResponse.class);
                logger.info("✅ ORDER PLACED! ID: {} Status: {}", ok.getOrderId(), ok.getStatus());
                return ok;
            } else {
                BinanceError err = mapper.readValue(body, BinanceError.class);
                logger.error("⛔ ORDER REJECTED! HTTP={} Code: {} Msg: {}",
                        response.statusCode(), err.getCode(), err.getMsg());
                return null;
            }

        } catch (Exception e) {
            logger.error("💥 Critical Error in EnterOrderService", e);
            return null;
        }
    }
}