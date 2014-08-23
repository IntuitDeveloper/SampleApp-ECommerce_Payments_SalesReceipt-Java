package com.intuit.developer.sampleapp.ecommerce.domain;

import javax.persistence.*;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 6/24/14
 * Time: 12:58 PM
 */
@Entity
public class AppInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String appToken;

    @Column(nullable = false)
    private String consumerKey;

    @Column(nullable = false)
    private String consumerSecret;

    public AppInfo(String appToken, String consumerKey, String consumerSecret) {
        this.appToken = appToken;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
    }

    public AppInfo() {

    }

    public long getId() {
        return id;
    }

    public String getAppToken() {
        return appToken;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }
}
