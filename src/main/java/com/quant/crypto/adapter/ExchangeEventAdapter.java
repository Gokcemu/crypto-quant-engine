package com.quant.crypto.adapter;

/**
 * Common interface for all exchange adapters.
 * <p>
 * This contract ensures that the system can switch between different exchanges (e.g. Binance, Kraken)
 * without modifying the core strategy logic.
 * </p>
 */
public interface ExchangeEventAdapter {

    /**
     * Processes the raw message received from the exchange (WebSocket)
     * and converts it into a domain event for the strategy.
     *
     * @param rawMessage The raw JSON string payload received from the stream.
     */
    void processMessage(String rawMessage);
}