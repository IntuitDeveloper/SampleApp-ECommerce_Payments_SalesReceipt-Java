package com.intuit.ipp.internal.core;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.exception.BadRequestException;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.ServiceException;
import com.intuit.ipp.interceptors.*;
import com.intuit.ipp.net.MethodType;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public final class ApiInvoker implements IApiInvoker
{
    private static final org.slf4j.Logger logger = Utils.getPrefixedLogger(ApiInvoker.class.getSimpleName());

    private static final String contentType = "application/json";

    public static enum HTTPMethod {
        POST, GET
    }

    private static class ApiInvokerLoader {
        private static final ApiInvoker INSTANCE = new ApiInvoker();
    }

    private ApiInvoker() {
        if (ApiInvokerLoader.INSTANCE != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    public static ApiInvoker getInstance() {
            return ApiInvokerLoader.INSTANCE;
    }

    public ApiInvokerResponse processRequest(ApiInvokerRequest apiInvokerRequest) throws FMSException
    {
        IntuitMessage intuitMessage = new IntuitMessage();
        RequestElements requestElements = intuitMessage.getRequestElements();

        requestElements.setContext(apiInvokerRequest.getContext());

        Map<String, String> requestParameters = requestElements.getRequestParameters();
        String uri = apiInvokerRequest.getURI() + buildQuery(apiInvokerRequest.getQueryParams());
        requestParameters.put(RequestElements.REQ_PARAM_RESOURCE_URL, uri);

        HTTPMethod httpMethod = apiInvokerRequest.getHttpMethod();
        if (httpMethod == HTTPMethod.GET) {
            requestParameters.put(RequestElements.REQ_PARAM_METHOD_TYPE, MethodType.GET.toString());
        } else if (httpMethod == HTTPMethod.POST) {
            requestParameters.put(RequestElements.REQ_PARAM_METHOD_TYPE, MethodType.POST.toString());
        }

        buildRequestHeader(requestElements.getRequestHeaders(), apiInvokerRequest.getHeaderParams(), apiInvokerRequest.getContext());

        requestElements.setPostString(apiInvokerRequest.getPayload());
        // TODO - just used for logging, should fix in Interceptor
//        requestElements.setSerializedData(apiInvokerRequest.getPayload());

        QuickBooksInterceptorProvider connectionProvider = new QuickBooksInterceptorProvider();
        connectionProvider.executeInterceptors(intuitMessage);

        return buildResponse(intuitMessage.getResponseElements());
    }

    private String escapeString(String str) {
        try{
            return URLEncoder.encode(str, "utf8").replaceAll("\\+", "%20");
        }
        catch(UnsupportedEncodingException e) {
            return str;
        }
    }

    // TODO - this should be improved
    private String buildQuery(Map<String, String> queryParams) throws BadRequestException
    {
        StringBuilder b = new StringBuilder();

        for(String key : queryParams.keySet()) {
            String value = queryParams.get(key);
            if (value != null){
                if(b.toString().length() == 0)
                    b.append("?");
                else
                    b.append("&");
                b.append(escapeString(key)).append("=").append(escapeString(value));
            }
        }

        return b.toString();
    }

    private void buildRequestHeader(Map<String, String> requestHeaders, Map<String, String> headerParams, Context context)
    {
        // request specific header items
        for(String key : headerParams.keySet()) {
            requestHeaders.put(key, headerParams.get(key));
        }

        // Add standard Intuit items
        UUID trackingId = context.getTrackingID();
        if (trackingId != null) {
            String intuit_tid = trackingId.toString();
            if (!StringUtils.isBlank(intuit_tid)) {
                requestHeaders.put(RequestElements.HEADER_INTUIT_TID, intuit_tid);
            }
        }

        requestHeaders.put(RequestElements.HEADER_PARAM_CONTENT_TYPE, contentType);
    }

    private ApiInvokerResponse buildResponse(ResponseElements httpResponse) throws ServiceException
    {
        ApiInvokerResponse response = new ApiInvokerResponse();
        response.setHttpStatusCode(httpResponse.getStatusCode());
        response.setPayload(httpResponse.getDecompressedData());

        return response;
    }
}

