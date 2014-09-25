package com.intuit.developer.sampleapp.ecommerce.test.unit.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.*;
import com.intuit.developer.sampleapp.ecommerce.qbo.PaymentGateway;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOServiceFactory;
import com.intuit.ipp.data.payment.Capture;
import com.intuit.ipp.data.payment.Charge;
import com.intuit.ipp.data.payment.Charge.ChargeStatus;
import com.intuit.ipp.services.payment.ChargeService;
import com.intuit.ipp.services.payment.RequestContext;
import mockit.*;
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
        final Charge chargeResponse1 = new Charge();
        chargeResponse1.setAmount(shoppingCart.getTotal().getAmount());
        chargeResponse1.setStatus(ChargeStatus.AUTHORIZED);
        chargeResponse1.setDescription(PaymentGateway.CHARGE_DESCRIPTION);
        chargeResponse1.setId("14553");
        final Charge chargeResponse2 = new Charge();
        chargeResponse1.setAmount(shoppingCart.getTotal().getAmount());

        //
        // Setup strict expectations (mocking, but order matters)
        //
        new NonStrictExpectations(){{
            qboServiceFactory.getChargeService(withAny(new Company()));
            result = chargeService;

            qboServiceFactory.getChargeService(withAny(new Company()));
            result = chargeService;

            chargeService.charge(withAny(new RequestContext()), withAny(new Charge()));
            result = chargeResponse1;

            chargeService.capture(withAny(new RequestContext()), anyString, withAny(new Capture()));
            result = chargeResponse2;
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
            Charge chargePassedForCharge;
            chargeService.charge(null, chargePassedForCharge = withCapture());
            assertFalse(chargePassedForCharge.getCapture());
            assertEquals(shoppingCart.getTotal().getAmount(), chargePassedForCharge.getAmount());
            assertEquals(PaymentGateway.CHARGE_DESCRIPTION, chargePassedForCharge.getDescription());

            // Verify that the capture was called with the right parameters.
            Capture capturePassedForCapture;
            chargeService.capture(null, chargeResponse1.getId(), capturePassedForCapture = withCapture());
            assertEquals(shoppingCart.getTotal().getAmount(), capturePassedForCapture.getAmount());
            assertEquals(PaymentGateway.CHARGE_DESCRIPTION, capturePassedForCapture.getDescription());
        }};
    }
}