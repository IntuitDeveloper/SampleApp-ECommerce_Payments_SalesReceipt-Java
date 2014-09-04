package com.intuit.developer.sampleapp.ecommerce.domain;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.persistence.*;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ShoppingCart {

    public static final double SHIPPING_PERCENTAGE = .05d;
    public static final double TAX_PERCENTAGE = .0793d;
    public static final double PROMOTION_PERCENTAGE = .2d;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	long id;
	
	@OneToOne(optional=false)
	@JoinColumn(name="customer_fk", referencedColumnName="id")
	Customer customer;
	
	@OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="shoppingCart")
	List<CartItem> cartItems = new ArrayList<CartItem>();

	protected ShoppingCart()
	{
	}
	
	public ShoppingCart(Customer cust)
	{
		this.customer = cust;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public List<CartItem> getCartItems() {
		return cartItems;
	}

	public void setCartItems(List<CartItem> cartItems) {
		this.cartItems = cartItems;
	}
	
	public void addToCart(CartItem cartItem)
	{
		cartItems.add(cartItem);
	}

    public Money getSubTotal() {
        Money subTotal = Money.zero(CurrencyUnit.USD);
        for (CartItem cartItem : cartItems) {
            // subtotal = subtotal + unitPrice * qty
            subTotal = subTotal.plus(cartItem.getSalesItem().getUnitPrice().multipliedBy(cartItem.getQuantity()));
        }
        return subTotal;
    }

    public Money getPromotionSavings() {
        return getSubTotal().multipliedBy(PROMOTION_PERCENTAGE, RoundingMode.CEILING);
    }

    public Money getTax() {
        return getSubTotal().minus(getPromotionSavings()).multipliedBy(TAX_PERCENTAGE, RoundingMode.CEILING);
    }

	public Money getShipping() {
		return getSubTotal().minus(getPromotionSavings()).multipliedBy(SHIPPING_PERCENTAGE, RoundingMode.FLOOR);
	}

    public Money getTotal() {
        return getSubTotal().minus(getPromotionSavings()).plus(getTax()).plus(getShipping());
    }

}

