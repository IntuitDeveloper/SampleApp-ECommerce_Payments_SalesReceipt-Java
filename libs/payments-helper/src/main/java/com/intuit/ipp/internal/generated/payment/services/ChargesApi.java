package com.intuit.ipp.internal.generated.payment.services;


import com.intuit.ipp.core.Context;
import com.intuit.ipp.data.payment.Capture;
import com.intuit.ipp.data.payment.Charge;
import com.intuit.ipp.data.payment.Refund;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.internal.core.*;
import com.intuit.ipp.internal.core.ApiInvoker;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;

import java.util.*;

public class ChargesApi extends ServiceBase {

    private static final String baseURL = Utils.getPaymentAPIBaseURL();

    private static final Logger logger = Utils.getPrefixedLogger(ChargesApi.class.getSimpleName());

    public Charge create(Context context, String requestId, Charge body) throws FMSException {
        //switch to string builder
        // create path and map variables
        String path = "/charges".replaceAll("\\{format\\}", "json");
        String uri = baseURL + path;

        // query params
        Map<String, String> queryParams = new HashMap<String, String>();
        Map<String, String> headerParams = new HashMap<String, String>();

        addRequestId(requestId, headerParams);

        TypeReference<Charge> typeReference = new TypeReference<Charge>() {};

        ServiceRequest serviceRequest = createServiceRequest(context,uri,typeReference,headerParams,
                queryParams,body, ApiInvoker.HTTPMethod.POST);
        ServiceResponse<Charge> serviceResponse = super.sendRequest(serviceRequest);
        return serviceResponse.getResponse();
    }

     public Charge capture(Context context, String requestId, String chargeId, Capture capture) throws FMSException
    {
        String path = "/charges/{id}/capture".replaceAll("\\{format\\}","json").replaceAll("\\{" + "id" + "\\}",chargeId);
        String uri = baseURL + path;

        Map<String, String> queryParams = new HashMap<String, String>();
        Map<String, String> headerParams = new HashMap<String, String>();

        addRequestId(requestId, headerParams);

        TypeReference<Charge> typeReference = new TypeReference<Charge>() {};

        ServiceRequest serviceRequest = createServiceRequest(context, uri, typeReference, headerParams,
                queryParams, capture, ApiInvoker.HTTPMethod.POST);
        ServiceResponse<Charge> serviceResponse = super.sendRequest(serviceRequest);
        return serviceResponse.getResponse();
    }

    public Charge retrieve(Context context, String chargeId) throws FMSException{
        // create path and map variables
        String path = "/charges/{id}".replaceAll("\\{format\\}","json").replaceAll("\\{" + "id" + "\\}",chargeId.toString());
        String uri = baseURL + path;

        Map<String, String> queryParams = new HashMap<String, String>();
        Map<String, String> headerParams = new HashMap<String, String>();

        TypeReference<Charge> typeReference = new TypeReference<Charge>() {};

        ServiceRequest serviceRequest = createServiceRequest(context,uri,typeReference,headerParams,
                queryParams,null, ApiInvoker.HTTPMethod.GET);
        ServiceResponse<Charge> serviceResponse = super.sendRequest(serviceRequest);
        return serviceResponse.getResponse();
    }


    public Refund refund(Context context, String requestId, String chargeId, Refund refundrequest) throws FMSException{
        String path = "/charges/{id}/refunds".replaceAll("\\{format\\}","json").replaceAll("\\{" + "id" + "\\}",chargeId);
        String uri = baseURL + path;

        Map<String, String> queryParams = new HashMap<String, String>();
        Map<String, String> headerParams = new HashMap<String, String>();

        addRequestId(requestId, headerParams);

        TypeReference<Refund> typeReference = new TypeReference<Refund>() {};

        ServiceRequest serviceRequest = createServiceRequest(context,uri,typeReference,headerParams,
                queryParams,refundrequest, ApiInvoker.HTTPMethod.POST);
        ServiceResponse<Refund> serviceResponse = super.sendRequest(serviceRequest);
        return serviceResponse.getResponse();
    }
    
    
	public Refund getRefund(Context context, String chargeId, String refundId) throws FMSException {

		String path = "/charges/{id}/refunds/{refund_id}".replaceAll("\\{format\\}", "json")
				.replaceAll("\\{" + "id" + "\\}", chargeId.toString())
				.replaceAll("\\{" + "refund_id" + "\\}", refundId);
		String uri = baseURL + path;

		Map<String, String> queryParams = new HashMap<String, String>();
		Map<String, String> headerParams = new HashMap<String, String>();

		TypeReference<Refund> typeReference = new TypeReference<Refund>() {
		};

		ServiceRequest serviceRequest = createServiceRequest(context, uri, typeReference, headerParams, queryParams,
				null, ApiInvoker.HTTPMethod.GET);
		ServiceResponse<Refund> serviceResponse = super.sendRequest(serviceRequest);
		return serviceResponse.getResponse();
	}

    private void addRequestId(String requestId, Map<String, String> headerParams) {
        headerParams.put("Request-Id", requestId);
    }

    private  ServiceRequest  createServiceRequest(Context context, String uri,
                                        TypeReference<?> typeReference,
                                        Map<String,String> header,Map<String,String> query,
                                        Object requestObject,ApiInvoker.HTTPMethod httpMethod){
        ServiceRequest serviceRequest = new ServiceRequest();
        serviceRequest.setTypeReference(typeReference);
        serviceRequest.setUri(uri);
        serviceRequest.setContext(context);
        serviceRequest.setHeaders(header);
        serviceRequest.setQueryParam(query);
        serviceRequest.setRequestObject(requestObject);
        serviceRequest.setHttpMethod(httpMethod);
        return serviceRequest;
    }
}


