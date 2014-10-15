package com.intuit.developer.sampleapp.ecommerce.oauth;

import java.io.IOException;

/**
 * TODO: add OAuthException overview
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
