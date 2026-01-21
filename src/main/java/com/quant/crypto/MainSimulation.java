package com.quant.crypto;

import com.quant.crypto.adapter.BinanceEventAdapter;
import com.quant.crypto.model.OrderRequest;
import com.quant.crypto.model.OrderResponse;
import com.quant.crypto.service.OrderService;
import com.quant.crypto.service.StrategyService;

public class MainSimulation {

    public static void main(String[] args) {

        // 1. Mock Order Service (Simulates Exchange)
        // We use a lambda to mock the 'placeOrder' method.
        OrderService<OrderRequest, OrderResponse> mockOrderService = request -> {
            System.out.println(">>> [EXCHANGE] Order Received: " + request.getSide() + " " + request.getSymbol());
            // Return a successful dummy response
            return new OrderResponse("999111", "FILLED", "0.001");
        };

        // 2. Init Strategy (Threshold: $20,000)
        StrategyService strategy = new StrategyService(mockOrderService, "BTCUSDT", 20000.0);

        // 3. Init Adapter (The Bridge)
        BinanceEventAdapter adapter = new BinanceEventAdapter(strategy);

        System.out.println("Simulation Started...");

        // --- TEST CASE 1: High Price (Should NOT Buy) ---
        // Price: 25000 (Above 20000)
        String jsonHigh = """
            {
              "e": "aggTrade",
              "s": "BTCUSDT",
              "p": "25000.00",
              "q": "0.5",
              "T": 1672515782136,
              "m": true
            }
        """;
        System.out.println("\n[1] Processing High Price Event...");
        adapter.processMessage(jsonHigh);

        // --- TEST CASE 2: Low Price (Should BUY) ---
        // Price: 19500 (Below 20000) -> SIGNAL!
        String jsonLow = """
            {
              "e": "aggTrade",
              "s": "BTCUSDT",
              "p": "19500.00",
              "q": "1.2",
              "T": 1672515799999,
              "m": false
            }
        """;
        System.out.println("\n[2] Processing Low Price Event...");
        adapter.processMessage(jsonLow);
    }
}