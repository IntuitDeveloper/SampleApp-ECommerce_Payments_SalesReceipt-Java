package com.intuit.developer.sampleapp.ecommerce.controllers;

/**
 * Represents a request to place an order.
 * Combines a shopping cart with a method of payment.
 *
 * When the order has been successfully processed the processed flag will be set to true
 *
 */
public class OrderRequest {

    long shoppingCartId;
    String paymentToken;

    public OrderRequest() {

    }

    public long getShoppingCartId() {
        return this.shoppingCartId;
    }

    public void setShoppingCartId(long shoppingCartId) {
        this.shoppingCartId = shoppingCartId;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }
}
