package com.intuit.ipp.internal.core;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.internal.core.ApiInvoker;
import org.codehaus.jackson.type.TypeReference;

import java.util.Map;


public class ServiceRequest {

    /**
     * uri for this API request
     */
    private String uri;
    /**
     * query parameter for this request if any in {@link java.util.Map} as key value pair
     */
    private Map<String, String> queryParam;
    /**
     * Header parameter for this request if any in {@link java.util.Map} as key value pair
     */
    private Map<String, String> headers;
    /**
     * Request object that we would serialize to create the payload
     */
    private Object requestObject;
    /**
     * {@link com.intuit.ipp.core.Context}
     */
    private Context context;
    /**
     * class type that will be used for the response deserialization
     */
    private TypeReference<?> typeReference;

    private ApiInvoker.HTTPMethod httpMethod;

    public ApiInvoker.HTTPMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(ApiInvoker.HTTPMethod httpMethod) {
        this.httpMethod = httpMethod;
    }
    //getter and setter

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setQueryParam(Map<String, String> queryParam) {
        this.queryParam = queryParam;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Object getRequestObject() {
        return requestObject;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setTypeReference(TypeReference<?> typeReference) {
        this.typeReference = typeReference;
    }

    public String getUri() {
        return uri;
    }

    public Map<String, String> getQueryParam() {
        return queryParam;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setRequestObject(Object requestObject) {
        this.requestObject = requestObject;
    }

    public Context getContext() {
        return context;
    }

    public TypeReference<?> getTypeReference() {
        return typeReference;
    }

}