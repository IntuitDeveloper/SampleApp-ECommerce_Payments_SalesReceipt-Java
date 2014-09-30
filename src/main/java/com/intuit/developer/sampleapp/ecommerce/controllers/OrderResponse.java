package com.intuit.developer.sampleapp.ecommerce.controllers;
import com.intuit.ipp.data.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by akuchta on 9/4/14.
 */
public class OrderResponse {
    private String orderNumber;
    private BigDecimal total;
    private PhysicalAddress billAddr;
    private PhysicalAddress shipAddr;
    private String paymentType;
    private List<Line> lines;
    private ReferenceType customerRef;
    private TxnTaxDetail txnTaxDetail;
    private String txnId;

    public static OrderResponse fromSalesReceipt(SalesReceipt salesReceipt) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderNumber(salesReceipt.getDocNumber());
        orderResponse.setTotal(salesReceipt.getTotalAmt());
        orderResponse.setBillAddr(salesReceipt.getBillAddr());
        orderResponse.setShipAddr(salesReceipt.getShipAddr());
        orderResponse.setPaymentType(salesReceipt.getPaymentMethodRef().getName());
        orderResponse.setLines(salesReceipt.getLine());
        orderResponse.setCustomerRef(salesReceipt.getCustomerRef());
        orderResponse.setTxnTaxDetail(salesReceipt.getTxnTaxDetail());
        orderResponse.setTxnId(salesReceipt.getId());
        return orderResponse;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setBillAddr(PhysicalAddress billAddr) {
        this.billAddr = billAddr;
    }

    public PhysicalAddress getBillAddr() {
        return billAddr;
    }

    public void setShipAddr(PhysicalAddress shipAddr) {
        this.shipAddr = shipAddr;
    }

    public PhysicalAddress getShipAddr() {
        return shipAddr;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public List<Line> getLines() {
        return lines;
    }

    public void setCustomerRef(ReferenceType customerRef) {
        this.customerRef = customerRef;
    }

    public ReferenceType getCustomerRef() {
        return customerRef;
    }

    public void setTxnTaxDetail(TxnTaxDetail txnTaxDetail) {
        this.txnTaxDetail = txnTaxDetail;
    }

    public TxnTaxDetail getTxnTaxDetail() {
        return txnTaxDetail;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public String getTxnId() {
        return txnId;
    }
}
