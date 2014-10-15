package com.intuit.developer.sampleapp.ecommerce.domain;

import org.springframework.data.rest.core.config.Projection;

/**
 * Created by rnorian on 8/27/14.
 */
@Projection(name = "summary", types = CartItem.class)
public interface CartItemSummaryProjection {
    int getQuantity();
    SalesItem getSalesItem();
}
