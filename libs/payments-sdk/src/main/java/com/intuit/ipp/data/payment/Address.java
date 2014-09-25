package com.intuit.ipp.data.payment;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public class Address {
  
    private String streetAddress = null;
    private String city = null;
    private String region = null;
    private String country = null;
    private String postalCode = null;
    /**
     * Full street adddress, which may include house number and street name (CR acceptable)
     *
     * @return Full street adddress, which may include house number and street name (CR acceptable)
     */
    public String getStreetAddress() {
        return streetAddress;
    }

    /**
     * Full street adddress, which may include house number and street name (CR acceptable)
     *
     * @param streetAddress Full street adddress, which may include house number and street name (CR acceptable)
     */
    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    /**
     * City
     *
     * @return City
     */
    public String getCity() {
        return city;
    }

    /**
     * City
     *
     * @param city City
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Region within a country.  State, province, prefecture or region component.
     *
     * @return Region within a country.  State, province, prefecture or region component.
     */
    public String getRegion() {
        return region;
    }

    /**
     * Region within a country.  State, province, prefecture or region component.
     *
     * @param region Region within a country.  State, province, prefecture or region component.
     */
    public void setRegion(String region) {
        this.region = region;
    }

    /**
     * Country code or name
     *
     * @return Country code or name
     */
    public String getCountry() {
        return country;
    }

    /**
     * Country code or name
     *
     * @param country Country code or name
     */
    public void setCountry(String country) {
        this.country = country;
    }

    /**
     * Postal Code
     *
     * @return Postal Code
     */
    public String getPostalCode() {
        return postalCode;
    }

    /**
     * Postal Code
     *
     * @param postalCode Postal Code
     */
    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}

