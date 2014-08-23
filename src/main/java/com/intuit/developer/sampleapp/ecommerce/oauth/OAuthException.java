package com.intuit.developer.sampleapp.ecommerce.oauth;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: russellb337
 * Date: 7/17/14
 * Time: 9:57 AM
 */
public class OAuthException extends RuntimeException {

    public OAuthException(Throwable cause) {
        super(cause);
    }

    public OAuthException(String message) {
        super(message);
    }

    public OAuthException(String message, IOException cause) {
        super(message, cause);
    }
}
