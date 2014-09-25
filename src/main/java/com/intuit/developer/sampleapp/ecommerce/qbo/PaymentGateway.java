package com.intuit.developer.sampleapp.ecommerce.qbo;

import com.intuit.developer.sampleapp.ecommerce.domain.ShoppingCart;
import com.intuit.ipp.data.payment.Capture;
import com.intuit.ipp.data.payment.Charge;
import com.intuit.ipp.data.payment.Charge.ChargeStatus;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.payment.ChargeService;
import com.intuit.ipp.services.payment.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * This class contains methods to interface with the Payments API via the payments SDK.
 * Created by akuchta on 8/28/14.
 */
public class PaymentGateway {
    @Autowired
    QBOServiceFactory qboServiceFactory;

    static public final String CHARGE_DESCRIPTION = "E-Commerce Sample App";

    /**
     * Authorizes a charge for to a credit card who information has been tokenized
     * Then captures the funds previously authorized. This is done in two calls to demonstrate the API,
     * It is possible to do this with one SDK method, but it is a common use case to authorize and capture as
     * separate steps in the transaction,
     * @param cart - the cart to create an order for
     * @param paymentToken - - the tokenized credit card information
     */
    public void chargeCustomerForOrder(ShoppingCart cart, String paymentToken) {
        ChargeService chargeService = qboServiceFactory.getChargeService(cart.getCustomer().getCompany());
        ChargeService chargeService1 = qboServiceFactory.getChargeService(cart.getCustomer().getCompany());

        /**
         * Authorizing the charge will verify the credit card account has the funds available and
         * subtract the amount from the card holder's balance.
         */
        Charge charge = authorizeForOrder(cart, paymentToken, chargeService);

        /**
         * In a typcial use case, there would be time and other operations between these two calls.
         * For example, capture of funds may wait until the order has bee fulfilled.
         * It is possible to perform both actions in one step if that its more relevant:
         */
        // Authorize and Capture at the same time
        // authorizeAndCaptureChargeForOrder(charge, cart, paymentToken, chargeService);

        if (charge.getStatus() != ChargeStatus.AUTHORIZED) {
            throw new RuntimeException("The credit card charge was not successfully authorized");
        }

        /**
         * Capturing the charge will actually transfer the funds from the card holder's account
         * and credit it to the merchants account.
         */
        charge = captureFundsForCharge(charge, chargeService1);
    }

    /**
     * Authorize and capture funds in one call. - Not actively used but included for reference.
     * @param cart - the cart to make a charge for - used to determine amount to charge
     * @param paymentToken - the tokenized credit card information
     * @param chargeService - the charge service to use for the transaction
     */
    private Charge authorizeAndCaptureChargeForOrder(ShoppingCart cart, String paymentToken, ChargeService chargeService) {
        Charge charge = new Charge();
        charge.setCreated(new Date());
        charge.setDescription(CHARGE_DESCRIPTION);
        charge.setCurrency("USD");
        charge.setAmount(cart.getTotal().getAmount());
        charge.setToken(paymentToken);
        charge.setCapture(true);

        // Create request id
        RequestContext requestContext = new RequestContext();

        try {
            return chargeService.charge(requestContext, charge);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Authorize a charge for the order amount
     * @param cart - the cart to make a charge for - used to determine amount to charge
     * @param paymentToken - the tokenized credit card information
     * @param chargeService - the charge service to use for the transaction
     */
    private Charge authorizeForOrder(ShoppingCart cart, String paymentToken, ChargeService chargeService) {
        Charge charge = new Charge();

        // Only Authorize funds in this stage - set to 'true' to authorize and capture in one step
        charge.setCapture(false);

        // We created the charge today
        charge.setCreated(new Date());

        // We also need to set currency
        charge.setCurrency("USD");

        // Set Description - what will the customer see on their statement?
        charge.setDescription(CHARGE_DESCRIPTION);

        // Set the charge amount
        charge.setAmount(cart.getTotal().getAmount());

        // Supply the credit card information in the form of a payment token
        charge.setToken(paymentToken);

        // Create request id
        RequestContext requestContext = new RequestContext();

        // Try to authorize the charge
        try {
            return chargeService.charge(requestContext,charge);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Capture funds for a previously authorized charge.
     * @param charge - a charge object to use, will be updated with return value from service
     * @param chargeService - the charge service to use for the transaction
     */
    private Charge captureFundsForCharge(Charge charge, ChargeService chargeService) {
        Capture capture = new Capture();

        // Capture the same amount as previously authorized - this may not be true in every application
        // The capture amount can be more or less than the authorization amount
        // But in this example they are the same
        capture.setAmount(charge.getAmount());

        // Set the date
        capture.setCreated(new Date());

        // Keep the description from the authorization. They do not have to be the same,
        //  but it makes sense in this situation
        capture.setDescription(charge.getDescription());

        // Create request id
        RequestContext requestContext = new RequestContext();

        // Try to capture the funds
        try {
            return chargeService.capture(requestContext, charge.getId(), capture);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }
    }
}
