package com.intuit.ipp.data.payment;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import java.util.Date;
import java.math.BigDecimal;

public class Charge {
  
    private String id = null;
    private Date created = null;
    private ChargeStatus status = null;
    public enum ChargeStatus { AUTHORIZED, DECLINED, CAPTURED, CANCELLED, REFUNDED, SETTLED, };
    private BigDecimal amount = null;
    private String currency = null;
    private String token = null;
    private Card card = null;
    private PaymentContext context = null;
    private Boolean capture = null;
    private String authCode = null;
    private Capture captureDetail = null;
    private Refund[] refundDetail = null;
    private String description = null;
    private String avsStreet = null;
    private String avsZip = null;
    private String cardSecurityCodeMatch = null;
    private String appType = null;

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
     * Status of the transaction
     *
     * @return Status of the transaction
     */
    public ChargeStatus getStatus() {
        return status;
    }

    /**
     * Status of the transaction
     *
     * @param status Status of the transaction
     */
    public void setStatus(ChargeStatus status) {
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
     * Three-letter ISO 4217 currency code representing the currency in which the charge was made
     *
     * @return Three-letter ISO 4217 currency code representing the currency in which the charge was made
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Three-letter ISO 4217 currency code representing the currency in which the charge was made
     *
     * @param currency Three-letter ISO 4217 currency code representing the currency in which the charge was made
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Opaque representation of the credit card associated with this charge, as returned by the token endpoint
     *
     * @return Opaque representation of the credit card associated with this charge, as returned by the token endpoint
     */
    public String getToken() {
        return token;
    }

    /**
     * Opaque representation of the credit card associated with this charge, as returned by the token endpoint
     *
     * @param token Opaque representation of the credit card associated with this charge, as returned by the token endpoint
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Credit card details associated with this charge
     *
     * @return Credit card details associated with this charge
     */
    public Card getCard() {
        return card;
    }

    /**
     * Credit card details associated with this charge
     *
     * @param card Credit card details associated with this charge
     */
    public void setCard(Card card) {
        this.card = card;
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
     * Capture flag
     *
     * @return Capture flag
     */
    public Boolean getCapture() {
        return capture;
    }

    /**
     * Capture flag
     *
     * @param capture Capture flag
     */
    public void setCapture(Boolean capture) {
        this.capture = capture;
    }

    /**
     * Authorization code. Available for uncaptured charges, only
     *
     * @return Authorization code. Available for uncaptured charges, only
     */
    public String getAuthCode() {
        return authCode;
    }

    /**
     * Authorization code. Available for uncaptured charges, only
     *
     * @param authCode Authorization code. Available for uncaptured charges, only
     */
    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    /**
     * Charge capture detail. Available for charges previously authorized
     *
     * @return Charge capture detail. Available for charges previously authorized
     */
    public Capture getCaptureDetail() {
        return captureDetail;
    }

    /**
     * Charge capture detail. Available for charges previously authorized
     *
     * @param captureDetail Charge capture detail. Available for charges previously authorized
     */
    public void setCaptureDetail(Capture captureDetail) {
        this.captureDetail = captureDetail;
    }

    /**
     * Details for one or more refunds against this charge
     *
     * @return Details for one or more refunds against this charge
     */
    public Refund[] getRefundDetail() {
        return refundDetail;
    }

    /**
     * Details for one or more refunds against this charge
     *
     * @param refundDetail Details for one or more refunds against this charge
     */
    public void setRefundDetail(Refund[] refundDetail) {
        this.refundDetail = refundDetail;
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

    public String getAvsStreet() {
        return avsStreet;
    }

    public void setAvsStreet(String avsStreet) {
        this.avsStreet = avsStreet;
    }

    public String getAvsZip() {
        return avsZip;
    }

    public void setAvsZip(String avsZip) {
        this.avsZip = avsZip;
    }

    public String getCardSecurityCodeMatch() {
        return cardSecurityCodeMatch;
    }

    public void setCardSecurityCodeMatch(String cardSecurityCodeMatch) {
        this.cardSecurityCodeMatch = cardSecurityCodeMatch;
    }

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}
    
    
    
    
}

