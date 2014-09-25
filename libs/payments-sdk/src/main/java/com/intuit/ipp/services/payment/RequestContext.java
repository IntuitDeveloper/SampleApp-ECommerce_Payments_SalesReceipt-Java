package com.intuit.ipp.services.payment;

import org.apache.commons.lang.StringUtils;

import java.util.UUID;

/**
 * Per request context for payments api
 */
public class RequestContext {

    private final String requestId;

    /**
     * Default constructor
     *
     * A request id will be generated for the client
     */
    public RequestContext() {
        this.requestId = UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Constructor for the client to supply a requestId
     *
     * @param requestId  This must be unique for each request made for a particular company
     *
     * @throws java.lang.IllegalArgumentException if requestId null
     */
    public RequestContext(String requestId) {

        if (StringUtils.isBlank(requestId)) {
            throw new IllegalArgumentException("requestId must not be null or an empty string");
        }

        this.requestId = requestId;
    }

    /**
     * Get requestId
     *
     * @return requestId
     */
    public String getRequestID() {
        return requestId;
    }

}
