package com.intuit.ipp.data.payment;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class Card {
  
    private String id = null;
    private String number = null;
    private String expMonth = null;
    private String expYear = null;
    private String cvc = null;
    private String name = null;
    private Address address = null;
    private String commercialCardCode = null;
    private CardPresent cardPresent = null;
    private String cardType = null;
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
     * Credit/debit card number
     *
     * @return Credit/debit card number
     */
    public String getNumber() {
        return number;
    }

    /**
     * Credit/debit card number
     *
     * @param number Credit/debit card number
     */
    public void setNumber(String number) {
        this.number = number;
    }

    /**
     * Two digits indicating card's expiration month
     *
     * @return Two digits indicating card's expiration month
     */
    public String getExpMonth() {
        return expMonth;
    }

    /**
     * Two digits indicating card's expiration month
     *
     * @param expMonth Two digits indicating card's expiration month
     */
    public void setExpMonth(String expMonth) {
        this.expMonth = expMonth;
    }

    /**
     * Four digits indicating card's expiration year
     *
     * @return Four digits indicating card's expiration year
     */
    public String getExpYear() {
        return expYear;
    }

    /**
     * Four digits indicating card's expiration year
     *
     * @param expYear Four digits indicating card's expiration year
     */
    public void setExpYear(String expYear) {
        this.expYear = expYear;
    }

    /**
     * CVC code - Strongly recommended for screening fraudulent transactions
     *
     * @return CVC code - Strongly recommended for screening fraudulent transactions
     */
    public String getCvc() {
        return cvc;
    }

    /**
     * CVC code - Strongly recommended for screening fraudulent transactions
     *
     * @param cvc CVC code - Strongly recommended for screening fraudulent transactions
     */
    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    /**
     * Cardholder's name as it appears on the card
     *
     * @return Cardholder's name as it appears on the card
     */
    public String getName() {
        return name;
    }

    /**
     * Cardholder's name as it appears on the card
     *
     * @param name Cardholder's name as it appears on the card
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Address
     *
     * @return Address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Address
     *
     * @param address Address
     */
    public void setAddress(Address address) {
        this.address = address;
    }

    /**
     * Specific code that is applicable when the card used is  a commercial card (corporate cards)
     *
     * @return Specific code that is applicable when the card used is  a commercial card (corporate cards)
     */
    public String getCommercialCardCode() {
        return commercialCardCode;
    }

    /**
     * Specific code that is applicable when the card used is  a commercial card (corporate cards)
     *
     * @param commercialCardCode Specific code that is applicable when the card used is  a commercial card (corporate cards)
     */
    public void setCommercialCardCode(String commercialCardCode) {
        this.commercialCardCode = commercialCardCode;
    }

    /**
     * Applies when the card is swiped using a card reader. Commonly used at a point of sale location
     *
     * @return Applies when the card is swiped using a card reader. Commonly used at a point of sale location
     */
    public CardPresent getCardPresent() {
        return cardPresent;
    }

    /**
     * Applies when the card is swiped using a card reader. Commonly used at a point of sale location
     *
     * @param cardPresent Applies when the card is swiped using a card reader. Commonly used at a point of sale location
     */
    public void setCardPresent(CardPresent cardPresent) {
        this.cardPresent = cardPresent;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }
    
    
}

