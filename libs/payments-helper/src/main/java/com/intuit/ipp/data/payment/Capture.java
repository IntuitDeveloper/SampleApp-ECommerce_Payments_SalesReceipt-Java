package com.intuit.ipp.data.payment;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Date;
import java.math.BigDecimal;
import com.intuit.ipp.data.payment.PaymentContext;
public class Capture {
  
    private Date created = null;
    private BigDecimal amount = null;
    private PaymentContext context = null;
    private String description = null;
    /**
     * Object create time, in ISO 8601 date-time format
     *
     * @return Object create time, in ISO 8601 date-time format
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Object create time, in ISO 8601 date-time format
     *
     * @param created Object create time, in ISO 8601 date-time format
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Amount of the transaction. Valid values for amount are in the range 0.00 through 99999.99
     *
     * @return Amount of the transaction. Valid values for amount are in the range 0.00 through 99999.99
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Amount of the transaction. Valid values for amount are in the range 0.00 through 99999.99
     *
     * @param amount Amount of the transaction. Valid values for amount are in the range 0.00 through 99999.99
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Optional additional data that will be stored with this charge
     *
     * @return Optional additional data that will be stored with this charge
     */
    public PaymentContext getContext() {
        return context;
    }

    /**
     * Optional additional data that will be stored with this charge
     *
     * @param context Optional additional data that will be stored with this charge
     */
    public void setContext(PaymentContext context) {
        this.context = context;
    }

    /**
     * Optional description that will be stored with this charge
     *
     * @return Optional description that will be stored with this charge
     */
    public String getDescription() {
        return description;
    }

    /**
     * Optional description that will be stored with this charge
     *
     * @param description Optional description that will be stored with this charge
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

