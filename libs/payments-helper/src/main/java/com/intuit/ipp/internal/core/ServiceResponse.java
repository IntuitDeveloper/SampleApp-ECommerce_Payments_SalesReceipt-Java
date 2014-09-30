package com.intuit.ipp.internal.core;

public class ServiceResponse<T> {
    /** httpstatus code*/
    private int httpStatusCode;
    /**Response from API call */
    private T response;


    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public T getResponse() {
        return response;
    }

    public void setResponse(T response) {
        this.response = response;
    }
}
