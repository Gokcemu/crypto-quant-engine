package com.quant.crypto.model.event;

import java.util.List;

/**
 * Represents an Order Book update event (Depth Diff).
 * <p>
 * Corresponds to the 'depthUpdate' payload in Binance WebSocket streams.
 * It contains a list of price levels that have changed (delta).
 * </p>
 */
public class OrderBookUpdateEvent implements MarketEvent {

    private String symbol;

    /**
     * The first update ID in the event (Binance field: "U").
     * Used for synchronization logic to ensure no packets are missed.
     */
    private long firstUpdateId;

    /**
     * The final update ID in the event (Binance field: "u").
     * The local order book's updateId should be updated to this value after processing.
     */
    private long lastUpdateId;

    private long timestamp;

    /**
     * List of bids (Buy orders) to be updated.
     * Only contains price levels that have changed.
     */
    private List<PriceLevel> bids;

    /**
     * List of asks (Sell orders) to be updated.
     * Only contains price levels that have changed.
     */
    private List<PriceLevel> asks;

    /**
     * No-args constructor.
     * Required by JSON deserializers (e.g., Jackson) to instantiate the class via reflection.
     */
    public OrderBookUpdateEvent() {
    }

    /**
     * All-args constructor for manual instantiation or testing.
     *
     * @param symbol        Trading pair (e.g. "BTCUSDT")
     * @param firstUpdateId First update ID in the packet
     * @param lastUpdateId  Last update ID in the packet
     * @param timestamp     Event time in epoch milliseconds
     * @param bids          List of updated bid levels
     * @param asks          List of updated ask levels
     */
    public OrderBookUpdateEvent(String symbol, long firstUpdateId, long lastUpdateId, long timestamp, List<PriceLevel> bids, List<PriceLevel> asks) {
        this.symbol = symbol;
        this.firstUpdateId = firstUpdateId;
        this.lastUpdateId = lastUpdateId;
        this.timestamp = timestamp;
        this.bids = bids;
        this.asks = asks;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    // --- GETTERS & SETTERS ---

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public long getFirstUpdateId() {
        return firstUpdateId;
    }

    public void setFirstUpdateId(long firstUpdateId) {
        this.firstUpdateId = firstUpdateId;
    }

    public long getLastUpdateId() {
        return lastUpdateId;
    }

    public void setLastUpdateId(long lastUpdateId) {
        this.lastUpdateId = lastUpdateId;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<PriceLevel> getBids() {
        return bids;
    }

    public void setBids(List<PriceLevel> bids) {
        this.bids = bids;
    }

    public List<PriceLevel> getAsks() {
        return asks;
    }

    public void setAsks(List<PriceLevel> asks) {
        this.asks = asks;
    }

    @Override
    public String toString() {
        return "OrderBookUpdateEvent{" +
                "symbol='" + symbol + '\'' +
                ", firstUpdateId=" + firstUpdateId +
                ", lastUpdateId=" + lastUpdateId +
                ", bidsCount=" + (bids != null ? bids.size() : 0) +
                ", asksCount=" + (asks != null ? asks.size() : 0) +
                '}';
    }
}