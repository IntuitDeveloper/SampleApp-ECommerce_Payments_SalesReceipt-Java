package com.intuit.developer.sampleapp.ecommerce.test.unit.qbo;

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

public class PaymentGatewayTest {

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

        // Some extra assertions to make sure the cart amounts are right
        assertEquals(Money.of(CurrencyUnit.USD, 13.50), shoppingCart.getSubTotal());
        // Taxes and discounts
        assertEquals(Money.of(CurrencyUnit.USD, 11.66), shoppingCart.getTotal());


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
            // There should one call to "authorize" - See matcher for checks
            chargeService.charge(withArgThat(new AuthorizeChargeMatcher(shoppingCart)));
            // There be another call to "capture" - See matcher for checks
            //  chargeService.capture(anyString, withArgThat(new CaptureMatcher(shoppingCart)));
        }};
    }

    public class AuthorizeChargeMatcher extends TypeSafeMatcher<Charge> {

        ShoppingCart shoppingCartToMatch;

        public AuthorizeChargeMatcher(ShoppingCart shoppingCart) {
            this.shoppingCartToMatch = shoppingCart;
        }

        @Override
        protected boolean matchesSafely(Charge charge) {
            // The charge should not be captured, just authorized
            assertFalse(charge.getCapture());
            assertEquals(shoppingCartToMatch.getTotal().getAmount(), charge.getAmount());
            assertEquals(PaymentGateway.CHARGE_DESCRIPTION,charge.getDescription());

            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("The charge did not meet expectations");
        }
    }

    public class CaptureMatcher extends TypeSafeMatcher<Capture> {

        ShoppingCart shoppingCartToMatch;

        public CaptureMatcher(ShoppingCart shoppingCart) {
            this.shoppingCartToMatch = shoppingCart;
        }
        @Override
        protected boolean matchesSafely(Capture capture) {
            assertEquals(shoppingCartToMatch.getTotal().getAmount(), capture.getAmount());
            assertEquals(PaymentGateway.CHARGE_DESCRIPTION, capture.getDescription());
            return true;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("The capture did not meet expectations");
        }
    }
}