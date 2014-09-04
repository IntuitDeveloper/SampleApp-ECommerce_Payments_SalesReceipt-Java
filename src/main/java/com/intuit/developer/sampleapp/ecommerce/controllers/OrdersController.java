package com.intuit.developer.sampleapp.ecommerce.controllers;

import com.intuit.developer.sampleapp.ecommerce.domain.ShoppingCart;
import com.intuit.developer.sampleapp.ecommerce.qbo.PaymentGateway;
import com.intuit.developer.sampleapp.ecommerce.qbo.QBOGateway;
import com.intuit.developer.sampleapp.ecommerce.repository.CustomerRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.SalesItemRepository;
import com.intuit.developer.sampleapp.ecommerce.repository.ShoppingCartRepository;
import com.intuit.ipp.data.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Handles requests to place orders
 */
@RestController
@RequestMapping("/orders")
public class OrdersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private ShoppingCartRepository shoppingCartRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SalesItemRepository salesItemRepository;

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private QBOGateway qboGateway;

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    @ResponseBody
    public Order createPurchaseRequest(@RequestBody final Order order) {
        ShoppingCart cart = shoppingCartRepository.findOne(order.getShoppingCartId());

        // Payments
        //paymentGateway.chargeCustomerForOrder(cart, order.getPaymentToken());

        // Accounting
        // We need to create sales receipts in order to manage inventory/ accounting
        qboGateway.createSalesReceiptInQBO(cart);

        order.setStatus(Order.OrderStatus.PROCESSED);
        return order;
    }
}
