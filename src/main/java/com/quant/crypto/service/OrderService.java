package com.quant.crypto.service;

/**
 * Generic interface for submitting orders to an exchange.
 *
 * @param <T> The type of the order request (e.g., OrderRequest, KrakenOrderRequest)
 * @param <R> The type of the order response (e.g., OrderResponse, KrakenResponse)
 */
public interface OrderService<T, R> {

    /**
     * Submits a new order to the exchange.
     *
     * @param request The object containing order details.
     * @return The response received from the exchange.
     */
    R placeOrder(T request);
}