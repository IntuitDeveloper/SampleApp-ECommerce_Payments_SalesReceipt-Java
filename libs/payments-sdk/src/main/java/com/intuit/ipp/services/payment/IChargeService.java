package com.intuit.ipp.services.payment;

import com.intuit.ipp.data.payment.Capture;
import com.intuit.ipp.data.payment.Charge;
import com.intuit.ipp.data.payment.Refund;
import com.intuit.ipp.exception.FMSException;

public interface IChargeService {
	/**
	 * Charge, or authorize a charge, on a credit card.
     * <p>
     * The Charge object passed as the parameter must be initialized with the following,
     * <ul>
     * <li>The amount to charge
     * <li>The currency to be used (use ISO 4217 three-letter code)
     * <li>One of
     * <ul>
     * <li>A token representing the credit card to be charged, the token should be obtained from the tokens resource
     * <li>A card object with details of the credit card to be charged. N.B. PCI compliance rules may apply if this option is used.
     * </ul>
     * <li>The capture flag set to null, or true to capture a charge immediately, or set to false to authorize a charge.
     * </ul>
     * <p>
     * The Charge object passed as the parameter can optionally have the following set,
     * <ul>
     * <li>PaymentContext, extra information that will be stored with the charge
     * <li>Description, a string that will be stored with the charge
     * </ul>
     * <p>
     * If your API key is in development mode, the supplied card won't
     * actually be charged, though everything else will occur as if in production mode.
	 *
     * @param requestContext data specific to a single request
	 * @param charge         details of the charge as described above
	 * @return complete description of charge newly created charge.
	 * @throws com.intuit.ipp.exception.AuthorizationException if HTTP status code is 401
     * @throws com.intuit.ipp.exception.BadRequestException    if there is an error in the client data
     * @throws com.intuit.ipp.exception.FMSException           for unexpected HTTP status
     * @throws com.intuit.ipp.exception.ServiceException       if there is an error on the server
	 *
	 */
	public Charge charge(RequestContext requestContext, Charge charge) throws FMSException;

	/**
	 * Retrieve details of a charge.
	 *
     * @param requestContext data specific to a single request
	 * @param chargeId       id of previously created charge
	 * @return complete description of charge
     * @throws com.intuit.ipp.exception.AuthorizationException if HTTP status code is 401
     * @throws com.intuit.ipp.exception.BadRequestException    if there is an error in the client data
     * @throws com.intuit.ipp.exception.FMSException           for unexpected HTTP status
     * @throws com.intuit.ipp.exception.ServiceException       if there is an error on the server
     *
	 */
	public Charge getCharge(RequestContext requestContext, String chargeId) throws FMSException;

 	/**
	 * Refund part or all of a charge.
     * <p>
     * The Refund object passed as the parameter must be initialized with the following,
     * <ul>
     * <li>The amount to refund
     * </ul>
     * <p>
     * The Refund object passed as the parameter can optionally have the following set,
     * <ul>
     * <li>PaymentContext, extra information that will be stored with the charge
     * <li>Description, a string that will be stored with the charge
     * </ul>
     *
     * @param requestContext data specific to a single request
     * @param chargeId       original charge id
	 * @param refund         details of refund as described above
	 * @return complete refund description
     * @throws com.intuit.ipp.exception.AuthorizationException if HTTP status code is 401
     * @throws com.intuit.ipp.exception.BadRequestException    if there is an error in the client data
     * @throws com.intuit.ipp.exception.FMSException           for unexpected HTTP status
     * @throws com.intuit.ipp.exception.ServiceException       if there is an error on the server
     *
	 */
	public Refund refund(RequestContext requestContext, String chargeId, Refund refund) throws FMSException;

	/**
	 * Retrieve details of a refund.
	 *
     * @param requestContext data specific to a single request
	 * @param chargeId       id of the original charge
	 * @param refundId       id of the refund
	 * @return  complete refund description
     * @throws com.intuit.ipp.exception.AuthorizationException if HTTP status code is 401
     * @throws com.intuit.ipp.exception.BadRequestException    if there is an error in the client data
     * @throws com.intuit.ipp.exception.FMSException           for unexpected HTTP status
     * @throws com.intuit.ipp.exception.ServiceException       if there is an error on the server
     *
	 */
	public Refund getRefund(RequestContext requestContext, String chargeId, String refundId) throws FMSException;

 	/**
	 * Capture the payment of an existing, uncaptured, charge
     * <p>
     * The Capture object passed as the parameter must be initialized with the following,
     * <ul>
     * <li>The amount to capture
     * </ul>
     * <p>
     * The Capture object passed as the parameter can optionally have the following set,
     * <ul>
     * <li>PaymentContext, extra information that will be stored with the charge
     * <li>Description, a string that will be stored with the charge
     * </ul>
	 *
     * @param requestContext data specific to a single request
	 * @param chargeId       id returned by the authorization (a previous charge where capture flag was set to false)
	 * @param capture        details of the capture as described above
	 * @return complete description of charge
     * @throws com.intuit.ipp.exception.AuthorizationException if HTTP status code is 401
     * @throws com.intuit.ipp.exception.BadRequestException    if there is an error in the client data
     * @throws com.intuit.ipp.exception.FMSException           for unexpected HTTP status
     * @throws com.intuit.ipp.exception.ServiceException       if there is an error on the server
     *
	 */
	public Charge capture(RequestContext requestContext, String chargeId, Capture capture) throws FMSException;

}
