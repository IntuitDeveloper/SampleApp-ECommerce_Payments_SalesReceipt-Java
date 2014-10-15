package com.intuit.developer.sampleapp.ecommerce.oauth;

/**
 * TODO: add CompanyRequestTokenSecret overview
 */
public class CompanyRequestTokenSecret {

    private final String appCompanyId;
    private final String requestTokenSecret;

    public CompanyRequestTokenSecret(String appCompanyId, String requestTokenSecret) {
        this.appCompanyId = appCompanyId;
        this.requestTokenSecret = requestTokenSecret;
    }

    public String getAppCompanyId() {
        return appCompanyId;
    }

    public String getRequestTokenSecret() {
        return requestTokenSecret;
    }
}
