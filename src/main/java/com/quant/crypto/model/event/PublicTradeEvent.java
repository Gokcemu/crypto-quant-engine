package com.quant.crypto.model.event;

/**
 * Represents a public trade event received from the exchange (e.g. Binance aggTrade).
 * <p>
 * This class implements {@link MarketEvent} to allow generic processing in strategies.
 * </p>
 */
public class PublicTradeEvent implements MarketEvent {

    private String symbol;
    private double price;
    private double quantity;
    private long timestamp;

    /**
     * If true, the buyer was the maker.
     * This means the trade was triggered by a SELL order hitting the bid.
     */
    private boolean isBuyerMaker;

    /**
     * No-args constructor.
     * Required by JSON deserializers (like Jackson) to instantiate the class via reflection.
     */
    public PublicTradeEvent() {
    }

    /**
     * All-args constructor for manual instantiation or testing.
     *
     * @param symbol       Trading pair (e.g. "BTCUSDT")
     * @param price        Execution price
     * @param quantity     Execution size
     * @param timestamp    Trade time in epoch milliseconds
     * @param isBuyerMaker True if buyer is maker (Sell aggressor), False if seller is maker (Buy aggressor)
     */
    public PublicTradeEvent(String symbol, double price, double quantity, long timestamp, boolean isBuyerMaker) {
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = timestamp;
        this.isBuyerMaker = isBuyerMaker;
    }

    // --- GETTERS ---

    public String getSymbol() {
        return symbol;
    }

    public double getPrice() {
        return price;
    }

    public double getQuantity() {
        return quantity;
    }

    /**
     * @return True if the buyer was the market maker (Passive side).
     * This implies the aggressor was a Seller.
     */
    public boolean isBuyerMaker() {
        return isBuyerMaker;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    // --- SETTERS (Used by JSON Mappers) ---

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setBuyerMaker(boolean buyerMaker) {
        isBuyerMaker = buyerMaker;
    }

    @Override
    public String toString() {
        return "PublicTradeEvent{" +
                "symbol='" + symbol + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", timestamp=" + timestamp +
                ", isBuyerMaker=" + isBuyerMaker +
                '}';
    }
}