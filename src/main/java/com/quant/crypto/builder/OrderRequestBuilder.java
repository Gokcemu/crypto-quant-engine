package com.quant.crypto.builder;

import com.quant.crypto.model.OrderRequest;
import com.quant.crypto.model.enums.OrderSide;
import com.quant.crypto.model.enums.OrderType;
import com.quant.crypto.model.enums.TimeInForce;

public class OrderRequestBuilder {

    // Varsayılan Değerler (Default Values)
    private String symbol = "BTCUSDT";
    private OrderSide side = OrderSide.BUY;
    private OrderType type = OrderType.MARKET;
    private String quantity = "0.001";
    private String price = null;
    private TimeInForce timeInForce = null;

    // 1. Static Factory Method: Başlangıç Noktası
    public static OrderRequestBuilder aMarketBuyOrder() {
        return new OrderRequestBuilder();
    }

    public static OrderRequestBuilder aLimitBuyOrder() {
        return new OrderRequestBuilder()
                .withType(OrderType.LIMIT)
                .withPrice("50000.00") // Güvenli bir default fiyat
                .withTimeInForce(TimeInForce.GTC);
    }

    // 2. "With" Metotları: Varsayılanları ezmek için
    public OrderRequestBuilder withSymbol(String symbol) {
        this.symbol = symbol;
        return this;
    }

    public OrderRequestBuilder withQuantity(String quantity) {
        this.quantity = quantity;
        return this;
    }

    public OrderRequestBuilder withType(OrderType type) {
        this.type = type;
        return this;
    }

    public OrderRequestBuilder withPrice(String price) {
        this.price = price;
        return this;
    }

    public OrderRequestBuilder withTimeInForce(TimeInForce tif) {
        this.timeInForce = tif;
        return this;
    }

    // 3. Build Metodu: Son ürünü paketleyip verir
    public OrderRequest build() {
        OrderRequest request = new OrderRequest(symbol, side, type, quantity);
        if (price != null) request.price(price);
        if (timeInForce != null) request.timeInForce(timeInForce);
        return request;
    }
}