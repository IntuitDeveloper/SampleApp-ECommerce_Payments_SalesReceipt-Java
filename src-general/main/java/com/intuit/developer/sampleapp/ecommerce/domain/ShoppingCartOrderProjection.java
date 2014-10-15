package com.intuit.developer.sampleapp.ecommerce.domain;

import org.joda.money.Money;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "order", types = ShoppingCart.class)
public interface ShoppingCartOrderProjection {
    long getId();
    Money getSubTotal();
    Money getPromotionSavings();
    Money getTax();
	Money getShipping();
    Money getTotal();
}
