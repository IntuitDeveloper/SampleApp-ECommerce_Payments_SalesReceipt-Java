package com.intuit.developer.sampleapp.ecommerce.controllers;

import javax.print.attribute.standard.JobKOctetsProcessed;

/**
 * Represents a request to place an order.
 * Combines a shopping cart with a method of payment.
 *
 * When the order has been successfully processed the processed flag will be set to true
 *
 */
public class Order {

    public enum OrderStatus {
        PENDING, PROCESSED
    }
    long shoppingCartId;
    String paymentToken;
    OrderStatus status;

    public Order() {

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

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
