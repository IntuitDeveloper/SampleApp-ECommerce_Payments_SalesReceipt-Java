package com.intuit.developer.sampleapp.ecommerce.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 10/1/14
 * Time: 9:51 AM
 */
@Entity
public class SystemProperty {

    @Id
    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String value;

    public SystemProperty() {

    }

    public SystemProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
