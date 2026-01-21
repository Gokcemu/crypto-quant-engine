package com.quant.crypto.model.event;

/**
 * Represents a single price level in the order book.
 * Contains the price and the quantity available at that price.
 */
public class PriceLevel {

    private double price;
    private double quantity;

    /**
     * No-args constructor for JSON deserialization.
     */
    public PriceLevel() {
    }

    /**
     * @param price    The price level
     * @param quantity The volume available (size)
     */
    public PriceLevel(double price, double quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "[" + price + ", " + quantity + "]";
    }
}