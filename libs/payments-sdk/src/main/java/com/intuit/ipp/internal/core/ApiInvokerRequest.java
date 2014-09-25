package com.intuit.ipp.internal.core;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.security.IAuthorizer;

import java.util.HashMap;
import java.util.Map;

/**
 * Required input to invoke REST service
 */
public class ApiInvokerRequest
{
    private String URI;
    private String payload;
    private ApiInvoker.HTTPMethod httpMethod;
    private Map<String, String> headerParams;
    private Map<String, String> queryParams;
    private Context context;

    public String getURI() {
        return URI;
    }

    public void setURI(String URI) {
        this.URI = URI;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public ApiInvoker.HTTPMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(ApiInvoker.HTTPMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Map<String, String> getHeaderParams()
    {
        if (headerParams == null)
        {
            headerParams = new HashMap<String, String>();
        }

        return headerParams;
    }

    public void setHeaderParams(Map<String, String> headerParams) {
        this.headerParams = headerParams;
    }

    public Map<String, String> getQueryParams()
    {
        if (queryParams == null)
        {
            queryParams = new HashMap<String, String>();
        }

        return queryParams;
    }

    public void setQueryParams(Map<String, String> queryParams) {
        this.queryParams = queryParams;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
