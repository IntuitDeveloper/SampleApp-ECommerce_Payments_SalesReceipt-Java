package com.intuit.developer.sampleapp.ecommerce.domain;

import javax.persistence.*;

@Entity
public class CartItem {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	long id;

	@ManyToOne(optional=false)
	@JoinColumn(name="item_fk", referencedColumnName="id")
	SalesItem salesItem;

	@ManyToOne(optional=false)
	@JoinColumn(name="shoppingcart_fk",referencedColumnName="id")
	ShoppingCart shoppingcart;

	int quantity;

	protected CartItem()
	{

	}

	public CartItem(SalesItem salesItem, int quantity, ShoppingCart shoppingCart)
	{
		this.salesItem = salesItem;
		this.quantity = quantity;
		this.shoppingcart = shoppingCart;
	}

	public ShoppingCart getShoppingcart() {
		return shoppingcart;
	}

	public void setShoppingcart(ShoppingCart shoppingcart) {
		this.shoppingcart = shoppingcart;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public SalesItem getSalesItem() {
		return salesItem;
	}

	public void setSalesItem(SalesItem salesItem) {
		this.salesItem = salesItem;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj instanceof CartItem)
		{
			if(this.salesItem.getId() == ((CartItem)obj).getSalesItem().getId())
				return true;
		}
		return false;
	}
}
