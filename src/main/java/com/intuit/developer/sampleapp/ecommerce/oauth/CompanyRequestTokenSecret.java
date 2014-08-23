package com.intuit.developer.sampleapp.ecommerce.oauth;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 7/22/14
 * Time: 8:55 AM
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
