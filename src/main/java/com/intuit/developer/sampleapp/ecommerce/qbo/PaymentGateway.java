package com.intuit.developer.sampleapp.ecommerce.qbo;


import com.intuit.developer.sampleapp.ecommerce.controllers.OrderConfirmation;
import com.intuit.developer.sampleapp.ecommerce.domain.ShoppingCart;
import com.intuit.ipp.data.Currency;
import com.intuit.ipp.data.payment.Capture;
import com.intuit.ipp.data.payment.Charge;
import com.intuit.ipp.data.payment.ChargeStatus;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.payment.ChargeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
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
     * seperate steps in the transaction,
     * @param cart
     * @param paymentToken
     */
    public void chargeCustomerForOrder(ShoppingCart cart, String paymentToken, OrderConfirmation confirmation) {
        ChargeService chargeService = qboServiceFactory.getChargeService(cart.getCustomer().getCompany());

        Charge charge = new Charge();

        authorizeAndCaptureChargeForOrder(charge, cart, paymentToken, chargeService);
//        /**
//         * Authorizing the charge will verify the credit card account has the funds available and
//         * subtract the amount from the card holder's balance.
//         */
//        authorizeChargeForOrder(charge, cart, paymentToken, chargeService);
//
//        /**
//         * In a typcial use case, there would be time and other operations between these two calls.
//         * For example, capture of funds may wait until the order has bee fulfilled.
//         */
//
//        if (charge.getStatus() != ChargeStatus.AUTHORIZED) {
//            throw new RuntimeException("The credit card charge was not successfully authorized");
//        }
//
//        /**
//         * Capturing the charge will actually transfer the funds from the card holder's account
//         * and credit it to the merchants account.
//         */
//        captureFundsForCharge(charge, chargeService);
//
//        if (charge.getStatus() != ChargeStatus.SETTLED) {
//            throw new RuntimeException("The card charge was not successfully settled");
//        }

    }

    private void authorizeAndCaptureChargeForOrder(Charge charge, ShoppingCart cart, String paymentToken, ChargeService chargeService) {
        charge.setCreated(new Date());
        charge.setDescription(CHARGE_DESCRIPTION);
        charge.setAmount(cart.getTotal().getAmount());
        charge.setToken(paymentToken);
        charge.setCapture(true);
        try {
            charge = chargeService.charge(charge);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void authorizeChargeForOrder(Charge charge, ShoppingCart cart, String paymentToken, ChargeService chargeService) {
        // Only Authorize funds in this stage - set to 'true' to authorize and capture in one step
        charge.setCapture(false);

        // We created the charge today
        charge.setCreated(new Date());

        // We may also need to set currency
        //charge.setCurrency("USD");

        // Set Description - what will the customer see on their statement?
        charge.setDescription(CHARGE_DESCRIPTION);

        // Set the charge amount
        charge.setAmount(cart.getTotal().getAmount());

        // Supply the credit card information in the form of a payment token
        charge.setToken(paymentToken);

        // Try to authorize the charge
        try {
            charge = chargeService.charge(charge);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void captureFundsForCharge(Charge charge, ChargeService chargeService) {

//        Capture capture = new Capture();
//
//        try {
//            charge = chargeService.capture(charge.getId(), capture);
//        } catch (FMSException e) {
//            throw new RuntimeException(e);
//        }
    }
}
