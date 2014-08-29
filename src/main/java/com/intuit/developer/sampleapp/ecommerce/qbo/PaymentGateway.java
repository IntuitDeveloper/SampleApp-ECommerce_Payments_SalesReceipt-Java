package com.intuit.developer.sampleapp.ecommerce.qbo;


import com.intuit.developer.sampleapp.ecommerce.domain.ShoppingCart;
import com.intuit.ipp.data.Currency;
import com.intuit.ipp.data.payment.Capture;
import com.intuit.ipp.data.payment.Charge;
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
    public void chargeCustomerForOrder(ShoppingCart cart, String paymentToken) {
        ChargeService chargeService = qboServiceFactory.getChargeService(cart.getCustomer().getCompany());

        Charge charge = new Charge();

        // We want to capture and authorize funds at the same time
        charge.setCapture(true);

        // We created the charge today
        charge.setCreated(new Date());

        // We may also need to set currency
        //charge.setCurrency("USD");

        // Set Description - what will the customer see on their statement?
        charge.setDescription(CHARGE_DESCRIPTION);
        charge.setAmount(cart.getTotal().getAmount());
        charge.setToken(paymentToken);

        try {
            chargeService.charge(charge);
        } catch (FMSException e) {
            throw new RuntimeException(e);
        }
    }
}
