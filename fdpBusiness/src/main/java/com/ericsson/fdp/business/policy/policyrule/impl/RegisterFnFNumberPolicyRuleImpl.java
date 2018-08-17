package com.ericsson.fdp.business.policy.policyrule.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.FnfUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This class checks whether the subscriber is already registered for Faf
 * service.
 * 
 * @author evasaty
 * 
 */
public class RegisterFnFNumberPolicyRuleImpl extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = 1L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RegisterFnFNumberPolicyRuleImpl.class);

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		boolean isFaFEnabled = false;
		FnfUtil fnfUtil = new FnfUtil();
		final Product product = fnfUtil.getProduct(fdpRequest);
		if (product != null) {
			isFaFEnabled = product.getIsFafType();
			if (isFaFEnabled && fnfUtil.isProductSpTypeValid(fdpRequest)) {
				Integer isAlreadyRegister = fnfUtil.IsSubscriberAlreadyRegisterForFaf(fdpRequest);
				if (FDPConstant.FAF_MSISDN_ALREADY_REGISTER == isAlreadyRegister) {
					LOGGER.error("Subscriber is already Registered." + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ this.getClass());
					fdpResponse = new FDPResponseImpl(Status.FAILURE, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_TERMINATE));
				} 
				if(FDPConstant.SERVICE_CLASS_IS_NOT_ELIGIBLE == isAlreadyRegister){
					LOGGER.error("Subscriber is not eligible for FAF service" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ this.getClass());
					fdpResponse = new FDPResponseImpl(Status.FAILURE, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), "Subscriber is not eligible for FAF service", TLVOptions.SESSION_TERMINATE));
				}
				if(FDPConstant.FAF_SC_BO_NOT_FOUND == isAlreadyRegister || FDPConstant.FAF_MSISDN_BASE_OFFER_NOT_FOUND == isAlreadyRegister){
					LOGGER.error("OfferID is not found FAF" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ this.getClass());
					fdpResponse = new FDPResponseImpl(Status.FAILURE, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), "Base offer is not configured for FAF", TLVOptions.SESSION_TERMINATE));
				}
				if(fnfUtil.getFafOfferIdForUpdateOffer(fdpRequest) == 0){
					LOGGER.error("Subscriber is not eligible for FaF." + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ this.getClass());
					fdpResponse = new FDPResponseImpl(Status.FAILURE, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), getNotification(fdpRequest), TLVOptions.SESSION_TERMINATE));
				}
			}
		}
		return fdpResponse;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {

		return null;
	}

	public String getNotificationText(FDPRequest fdpRequest) {
		return "Subscriber is already registered.";
	}

	public String getNotification(FDPRequest fdpRequest) {
		return "FAF offerID is not configured.";
	}

}
