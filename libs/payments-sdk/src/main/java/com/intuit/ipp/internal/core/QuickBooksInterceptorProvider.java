package com.intuit.ipp.internal.core;

import com.intuit.ipp.exception.FMSException;

import com.intuit.ipp.interceptors.DecompressionInterceptor;
import com.intuit.ipp.interceptors.HTTPClientConnectionInterceptor;
import com.intuit.ipp.interceptors.Interceptor;
import com.intuit.ipp.interceptors.IntuitMessage;
import com.intuit.ipp.util.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Class to assemble interceptors for communication with QuickBooks API
 *
 * TODO - should create a base class with common methods and variables for all interceptors
 * 
 */
public class QuickBooksInterceptorProvider {

	/**
	 * logger instance
	 */
	private static final org.slf4j.Logger LOG = Logger.getLogger();

	/**
	 * variable requestInterceptors is used for keeping the request interceptors
	 */
	private List<Interceptor> requestInterceptors = new ArrayList<Interceptor>();

	/**
	 * variable responseInterceptors is used for keeping the response interceptors
	 */
	private List<Interceptor> responseInterceptors = new ArrayList<Interceptor>();

	/**
	 * variable intuitMessage
	 */
	private IntuitMessage intuitMessage = null;

	/**
	 * Constructor QuickBooksInterceptorProvider
	 */
	public QuickBooksInterceptorProvider() {

		requestInterceptors.add(new HTTPClientConnectionInterceptor());

		responseInterceptors.add(new DecompressionInterceptor());
	}

	/**
	 * Method to execute the interceptors (request and response) which are added
	 *
	 * @param intuitMessage the intuit message
	 * @throws com.intuit.ipp.exception.FMSException the FMSException
	 */
	public void executeInterceptors(final IntuitMessage intuitMessage) throws FMSException {
		executeRequestInterceptors(intuitMessage);
		executeResponseInterceptors(intuitMessage);
	}

	/**
	 * Method to execute only request interceptors which are added to requestInterceptors list
	 *
	 * @param intuitMessage the intuit message
	 * @throws com.intuit.ipp.exception.FMSException the FMSException
	 */
	private void executeRequestInterceptors(final IntuitMessage intuitMessage) throws FMSException {
		Iterator<Interceptor> itr = requestInterceptors.iterator();
		while (itr.hasNext()) {
			Interceptor interceptor = itr.next();
			interceptor.execute(intuitMessage);
		}
	}

	/**
	 * Method to execute only response interceptors which are added to the responseInterceptors list
	 *
	 * @param intuitMessage the intuit message
	 * @throws com.intuit.ipp.exception.FMSException the FMSException
	 */
	private void executeResponseInterceptors(final IntuitMessage intuitMessage) throws FMSException {
		Iterator<Interceptor> itr = responseInterceptors.iterator();
		while (itr.hasNext()) {
			Interceptor interceptor = itr.next();
			interceptor.execute(intuitMessage);
		}
	}
}
