package com.intuit.ipp.internal.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;

import com.intuit.ipp.exception.AuthorizationException;
import com.intuit.ipp.exception.BadRequestException;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.exception.SerializationException;
import com.intuit.ipp.exception.ServiceException;

public class ServiceBase {

	private static final Logger LOGGER = Utils.getPrefixedLogger(ServiceBase.class.getSimpleName());

	public ServiceResponse sendRequest(ServiceRequest serviceRequest) throws FMSException {
		// input param validation context is required param ,
		if (serviceRequest.getContext() == null) {
			throw new IllegalArgumentException("Context is null");
		}
		// get and post diff
		// serialize
		String payload = null;
		if (serviceRequest.getHttpMethod().equals(ApiInvoker.HTTPMethod.POST)) {
			payload = JsonUtil.serialize(serviceRequest.getRequestObject());
		}

		// prepare ApiInvoker request
		ApiInvokerRequest request = createApiInvokerRequest(serviceRequest, payload);

		IApiInvoker apiInvoker = ApiInvoker.getInstance();
		ApiInvokerResponse response = apiInvoker.processRequest(request);

		if (response == null) {
			LOGGER.error("No response from ApiInvoker: it is null");
			throw new FMSException("Unexpected Error , service response object was null ");
		}

		int httpStatusCode = response.getHttpStatusCode();
		// may be a case when we do not have a valid body
		if (httpStatusCode >= HttpStatus.SC_OK && httpStatusCode <= 299) {
			LOGGER.info("API call succeeded");

			ServiceResponse serviceResponse = new ServiceResponse();
			serviceResponse.setHttpStatusCode(httpStatusCode);

			// there is a potential the response may not have an real payload;
			// being defensive or the request did not specify a response payload
			// type
			if (response.getPayload() != null && serviceRequest.getTypeReference() != null) {
				Object object = JsonUtil.deserialize(response.getPayload(), serviceRequest.getTypeReference());
				serviceResponse.setResponse(object);
			}
			return serviceResponse;

		} else {
			List<com.intuit.ipp.data.Error> errorList = null;
			if (StringUtils.isNotBlank(response.getPayload())) {
				try {
					errorList = createErrorList(response);
				} catch (SerializationException se) {
					LOGGER.debug("unable to deserialize to an error list", se);
					/**
					 * when a error thrown from any component outside of the
					 * actual backend - say at the apache layer or CTO the error
					 * format is not well defined. So specific exceptions like
					 * AuthroizationException never get thrown. So circumvent
					 * this and provide a better user experience, catch the
					 * issue and then create an error manually & set the http
					 * status code and also the response payload
					 */
				}
			}
			/**
			 * if there is no error list at this time, that means we were
			 * unsuccessful in creating the errorlist from the response. So
			 * create one manually and set the httpstatus code and the payload
			 * back
			 **/
			if (errorList == null) {
				errorList = new ArrayList<com.intuit.ipp.data.Error>();
				com.intuit.ipp.data.Error error = new com.intuit.ipp.data.Error();
				error.setCode("HttpStatusCode-" + httpStatusCode);
				error.setDetail("ResponsePayload: " + response.getPayload());
				errorList.add(error);
			}

			if (httpStatusCode == HttpStatus.SC_UNAUTHORIZED) {
				LOGGER.error("Authorization Error httpStatusCode {}", httpStatusCode);
				AuthorizationException exception = new AuthorizationException(errorList);
				throw exception;
			} else if (httpStatusCode >= HttpStatus.SC_BAD_REQUEST && httpStatusCode <= 499) {
				LOGGER.error("Bad Request Exception {}", httpStatusCode);
				BadRequestException badRequestException = new BadRequestException(errorList);
				LOGGER.debug("Client Side Error httpStatusCode {}", httpStatusCode);
				throw badRequestException;
			} else if (httpStatusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR && httpStatusCode <= 599) {
				LOGGER.error("Service exception {}", httpStatusCode);
				ServiceException serviceException = new ServiceException(errorList);
				throw serviceException;
			} else { // catch all status code
				LOGGER.error("unexpected exception {}", httpStatusCode);
				FMSException fmsException = new FMSException(errorList);
				throw fmsException;
			}

		}

	}

	private List<com.intuit.ipp.data.Error> createErrorList(ApiInvokerResponse apiInvokerResponse) throws SerializationException {
		TypeReference<Errors> typeReference = new TypeReference<Errors>() {
		};

		Errors errorContainer = (Errors) JsonUtil.deserialize(apiInvokerResponse.getPayload(),
				typeReference);

		List<Error> errorObjectList = errorContainer.getErrors();
		List<com.intuit.ipp.data.Error> errorList = new ArrayList<com.intuit.ipp.data.Error>();
		for (Error errorObject : errorObjectList) {
			com.intuit.ipp.data.Error error = new com.intuit.ipp.data.Error();

			error.setCode(errorObject.getCode());
			error.setMessage(errorObject.getMessage());
			error.setDetail(errorObject.getDetail());

			String element = String.format("HttpStatusCode=%s; Type=%s; MoreInfo=%s, InfoLink=%s",
					apiInvokerResponse.getHttpStatusCode(), errorObject.getType(), errorObject.getMoreInfo(),
					errorObject.getInfoLink());

			error.setElement(element);
			errorList.add(error);
		}
		return errorList;
	}

	private ApiInvokerRequest createApiInvokerRequest(ServiceRequest serviceRequest, String payload) {

		ApiInvokerRequest request = new ApiInvokerRequest();

		request.setContext(serviceRequest.getContext());
		request.setHeaderParams(serviceRequest.getHeaders());
		if (payload != null) {
			request.setPayload(payload);
		}
		request.setURI(serviceRequest.getUri());
		request.setQueryParams(serviceRequest.getQueryParam());
		request.setHttpMethod(serviceRequest.getHttpMethod());

		return request;
	}

}
