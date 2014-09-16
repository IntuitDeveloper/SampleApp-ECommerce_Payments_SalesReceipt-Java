package com.intuit.developer.sampleapp.ecommerce.test.unit.qbo;

import com.intuit.developer.sampleapp.ecommerce.controllers.OrderConfirmation;
import com.intuit.developer.sampleapp.ecommerce.domain.*;
import com.intuit.developer.sampleapp.ecommerce.qbo.PaymentGateway;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOServiceFactory;
import com.intuit.ipp.data.payment.Capture;
import com.intuit.ipp.data.payment.Charge;
import com.intuit.ipp.services.payment.ChargeService;
import mockit.*;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;
import static junit.framework.Assert.assertEquals;

public class PaymentGatewayUnitTests {

    @Tested
    PaymentGateway gateway;

    @Injectable
    QBOServiceFactory qboServiceFactory;

    @Mocked
    ChargeService chargeService;


    @Test
    public void testChargeCustomerForOrder() throws Exception {

        //
        // Setup test data
        //
        // Create a company
        Company company = new Company();
        company.setName("Foo");
        company.setAccessToken("asdasdA");
        company.setAccessTokenSecret("sfsfsdfsdfsdfs");

        // Create a customer
        Customer customer = new Customer("Bob", "Barker", "bob@barker.com", "945-000-6009");

        // Create a a shopping cart
        final ShoppingCart shoppingCart = new ShoppingCart(customer);

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

        //
        // Setup non-strict expectations (mocking)
        //
        new NonStrictExpectations() {{
            qboServiceFactory.getChargeService(withAny(new Company()));
            result = chargeService;
        }};

        //
        // Execute method under test
        //
       gateway.chargeCustomerForOrder(shoppingCart, "1235FF22345CD987741");

        //
        // Explicitly verify strict conditions. In ORDER because authorization must come before capture
        //
        new VerificationsInOrder() {{
            // Verify that the authorization was requested with the right parameters
            Charge chargePassed;
            chargeService.charge(chargePassed = withCapture());
            assertTrue(chargePassed.getCapture());
            assertEquals(shoppingCart.getTotal().getAmount(), chargePassed.getAmount());
            assertEquals(PaymentGateway.CHARGE_DESCRIPTION, chargePassed.getDescription());

            // Verify that the capture was request with the right parameters.
            // There be another call to "capture" - See matcher for checks
            //  chargeService.capture(anyString, withArgThat(new CaptureMatcher(shoppingCart)));
        }};
    }
}