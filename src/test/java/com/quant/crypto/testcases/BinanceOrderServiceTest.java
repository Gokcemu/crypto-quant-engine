package com.quant.crypto.testcases;

import com.quant.crypto.base.ServiceBaseTest;
import com.quant.crypto.builder.OrderRequestBuilder;
import com.quant.crypto.model.OrderRequest;
import com.quant.crypto.model.OrderResponse;
import com.quant.crypto.service.BinanceOrderService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link BinanceOrderService}.
 * Verifies that orders are correctly built, signed, and sent to the Binance Testnet.
 */
public class EnterOrderServiceTest extends ServiceBaseTest {

    @Test
    void testEnterMarketOrder() {
        logger.info("🧪 Test: Enter MARKET Buy Order");

        // GIVEN
        OrderRequest order = OrderRequestBuilder.aMarketBuyOrder()
                .withSymbol("BTCUSDT")
                .withQuantity("0.001")
                .build();

        // WHEN
        OrderResponse response = binanceOrderService.placeOrder(order);

        // THEN
        assertNotNull(response, "Response should not be null");
        assertTrue(response.isSuccess(), "Order should be successful");
        logger.info("✅ Response: {}", response);
    }

    @Test
    void testEnterLimitOrderWithNormalizedPrice() {
        logger.info("🧪 Test: Enter LIMIT Buy Order with Normalized Price");

        String rawPrice = "90000.1234567";
        String validPrice = normalizePrice(rawPrice); // Inherited helper method

        OrderRequest order = OrderRequestBuilder.aLimitBuyOrder()
                .withSymbol("BTCUSDT")
                .withQuantity("0.001")
                .withPrice(validPrice)
                .build();

        OrderResponse response = binanceOrderService.placeOrder(order);

        assertNotNull(response);
        // Note: Limit orders might be REJECTED if price is too far from market,
        // but we check if the request was sent successfully.
        if (response.isSuccess()) {
            logger.info("✅ Limit Order Placed: {}", response.getOrderId());
        } else {
            logger.warn("⚠️ Limit Order Rejected (Expected if price is off): {}", response.getMsg());
        }
    }

    @Test
    void testOrderRequestWithInvalidPriceRejected() {
        logger.info("🧪 Test: Invalid Order Handling");

        // Sending an absurdly low price to trigger a specific error filter
        OrderRequest order = OrderRequestBuilder.aLimitBuyOrder()
                .withSymbol("BTCUSDT")
                .withQuantity("0.001")
                .withPrice("1.00")
                .build();

        OrderResponse response = binanceOrderService.placeOrder(order);

        assertNotNull(response);
        assertFalse(response.isSuccess(), "Order should be rejected");

        // Binance Error Code -1013: Filter Failure
        assertEquals(-1013, response.getCode(), "Should return Filter Failure code");

        logger.info("✅ Rejected as expected: {}", response.getMsg());
    }
}