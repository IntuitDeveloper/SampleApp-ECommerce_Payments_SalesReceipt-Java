package com.intuit.developer.sampleapp.ecommerce.test.unit.domain;

import com.intuit.developer.sampleapp.ecommerce.domain.*;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ShoppingCartUnitTests {

    ShoppingCart shoppingCart;
    Company company;
    Customer customer;

    @Before
    public void setUp() throws Exception {
        //
        // Setup test data
        //
        // Create a company
        company = new Company();
        company.setName("Foo");
        company.setAccessToken("asdasdA");
        company.setAccessTokenSecret("sfsfsdfsdfsdfs");

        // Create a customer
        customer = new Customer("Bob", "Barker", "bob@barker.com", "945-000-6009");

        // Create a a shopping cart
        shoppingCart = new ShoppingCart(customer);

        // Create a list of items for the cart
        List<CartItem> cartItems = new ArrayList<>();
        CartItem cartItem = new CartItem();
        cartItem.setSalesItem(new SalesItem("ItemType1","It's an item", Money.of(CurrencyUnit.USD, 5.00), ""));
        cartItem.setQuantity(2);
        cartItem.setShoppingCart(shoppingCart);
        cartItems.add(cartItem);
        cartItem = new CartItem();
        cartItem.setSalesItem(new SalesItem("ItemType2","It's another item", Money.of(CurrencyUnit.USD, 3.50), ""));
        cartItem.setQuantity(1);
        cartItem.setShoppingCart(shoppingCart);
        cartItems.add(cartItem);
        shoppingCart.setCartItems(cartItems);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testGetSubTotal() throws Exception {
        assertEquals(Money.of(CurrencyUnit.USD, 13.50), shoppingCart.getSubTotal());
    }

    @Test
    public void testGetTotal() throws Exception {
        assertEquals(Money.of(CurrencyUnit.USD, 12.20), shoppingCart.getTotal());
    }
}