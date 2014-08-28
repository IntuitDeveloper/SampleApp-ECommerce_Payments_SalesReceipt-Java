package com.intuit.developer.sampleapp.ecommerce.domain;

import javax.persistence.*;
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
	
	@OneToMany(fetch = FetchType.LAZY, cascade=CascadeType.ALL, mappedBy="shoppingCart")
	List<CartItem> cartItems = new ArrayList<CartItem>();
	
	float subTotal;
	
	protected ShoppingCart()
	{
	}
	
	public ShoppingCart(Customer cust)
	{
		this.customer = cust;
	}
	
	public float getSubTotal() {
		return subTotal;
	}

	public void setSubTotal(float subTotal) {
		this.subTotal = subTotal;
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
	
}
