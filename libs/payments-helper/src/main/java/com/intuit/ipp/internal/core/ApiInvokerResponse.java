package com.intuit.ipp.internal.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Data returned by ApiInvoker
 */
public class ApiInvokerResponse
{
    private int httpStatusCode;
    private String payload;
    private Map<String, String> header;


    public Map<String, String> getHeader()
    {
        if (header == null)
        {
            header = new HashMap<String, String>();
        }

        return header;
    }

    public void setHeader(Map<String, String> header) {
        this.header = header;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
