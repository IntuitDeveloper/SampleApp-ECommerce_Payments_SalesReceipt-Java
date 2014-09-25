package com.intuit.ipp.services.payment;

import org.apache.commons.lang.StringUtils;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.data.payment.Capture;
import com.intuit.ipp.data.payment.Charge;
import com.intuit.ipp.data.payment.Refund;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.internal.generated.payment.services.ChargesApi;

public class ChargeService implements IChargeService {

    private final Context context;

    public ChargeService(Context context)
    {
        this.context = context;
    }

    @Override
    public Charge charge(RequestContext requestContext, Charge charge) throws FMSException {
        ChargesApi chargesApi = new ChargesApi();

        return chargesApi.create(context, requestContext.getRequestID(), charge);
    }

    @Override
    public Charge getCharge(RequestContext requestContext, String chargeId) throws FMSException {
        ChargesApi chargesApi = new ChargesApi();

        return chargesApi.retrieve(context, chargeId);
    }

    @Override
    public Refund refund(RequestContext requestContext, String chargeId, Refund refund) throws FMSException {

        ChargesApi chargesApi = new ChargesApi();

        return chargesApi.refund(context, requestContext.getRequestID(), chargeId, refund);
    }

	@Override
	public Refund getRefund(RequestContext requestContext, String chargeId, String refundId) throws FMSException {

		if (StringUtils.isBlank(chargeId)) {
			throw new IllegalArgumentException("chargeId cannot be empty or null");
		}

		if (StringUtils.isBlank(refundId)) {
			throw new IllegalArgumentException("refundId cannot be empty or null");
		}

		ChargesApi chargesApi = new ChargesApi();
		return chargesApi.getRefund(context, chargeId, refundId);
	}

    @Override
    public Charge capture(RequestContext requestContext, String chargeId, Capture capture) throws FMSException {
        ChargesApi chargesApi = new ChargesApi();

        return chargesApi.capture(context, requestContext.getRequestID(), chargeId, capture);
    }

}
