package com.intuit.developer.sampleapp.ecommerce.domain;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import javax.persistence.*;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ShoppingCart {
	
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
            subTotal = subTotal.plus(cartItem.getSalesItem().getUnitPrice());
        }
        return subTotal;
    }

    public Money getPromotionalSavings() {
        return getSubTotal().multipliedBy(.2d, RoundingMode.CEILING);
    }

    public Money getTax() {
        return getSubTotal().minus(getPromotionalSavings()).multipliedBy(.0793d, RoundingMode.CEILING);
    }

    public Money getTotal() {
        return getSubTotal().minus(getPromotionalSavings()).plus(getTax());
    }

}

