package com.intuit.ipp.internal.core;

import com.intuit.ipp.exception.FMSException;

/**
 * Invoke REST API
 */
public interface IApiInvoker
{
    ApiInvokerResponse processRequest(ApiInvokerRequest apiInvokerRequest) throws FMSException;
}
