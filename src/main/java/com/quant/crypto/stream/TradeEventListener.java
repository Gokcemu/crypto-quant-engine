package com.quant.crypto.model;

/**
 * Interface for listening to real-time market trade events.
 * Implementations of this interface can subscribe to the {@link com.quant.crypto.service.BinanceStreamClient}
 * to receive price updates.
 *
 * @author Gokcem Usul
 */
public interface TradeEventListener {

    /**
     * Triggered when a new trade occurs on the subscribed market stream.
     *
     * @param price     The price at which the trade was executed.
     * @param eventTime The timestamp of the trade event (in milliseconds).
     */
    void onTradeEvent(double price, long eventTime);
}