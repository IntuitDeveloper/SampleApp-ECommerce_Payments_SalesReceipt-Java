package com.intuit.ipp.data.payment;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Date;
import java.math.BigDecimal;

public class Refund {
  
    private String id = null;
    private Date created = null;
    private RefundStatus status = null;
    public enum RefundStatus { ISSUED, DECLINED, SETTLED, };
    private BigDecimal amount = null;
    private PaymentContext context = null;
    private String description = null;
    /**
     * System generated alpha-numeric id
     *
     * @return System generated alpha-numeric id
     */
    public String getId() {
        return id;
    }

    /**
     * System generated alpha-numeric id
     *
     * @param id System generated alpha-numeric id
     */
    public void setId(String id) {
        this.id = id;
    }

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
     * Status of this refund
     *
     * @return Status of this refund
     */
    public RefundStatus getStatus() {
        return status;
    }

    /**
     * Status of this refund
     *
     * @param status Status of this refund
     */
    public void setStatus(RefundStatus status) {
        this.status = status;
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
     * Optional additional data that will be stored with this refund
     *
     * @return Optional additional data that will be stored with this refund
     */
    public PaymentContext getContext() {
        return context;
    }

    /**
     * Optional additional data that will be stored with this refund
     *
     * @param context Optional additional data that will be stored with this refund
     */
    public void setContext(PaymentContext context) {
        this.context = context;
    }

    /**
     * Optional description that will be stored with this refund
     *
     * @return Optional description that will be stored with this refund
     */
    public String getDescription() {
        return description;
    }

    /**
     * Optional description that will be stored with this refund
     *
     * @param description Optional description that will be stored with this refund
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

