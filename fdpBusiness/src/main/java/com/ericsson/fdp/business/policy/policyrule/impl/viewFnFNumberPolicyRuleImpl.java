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

public class viewFnFNumberPolicyRuleImpl extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = 1L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(viewFnFNumberPolicyRuleImpl.class);

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		boolean isFaFEnabled = false;
		FnfUtil fnfUtil = new FnfUtil();
		final Product product = fnfUtil.getProduct(fdpRequest);
		if (product != null) {
			isFaFEnabled = product.getIsFafType();
			if (isFaFEnabled && fnfUtil.isProductSpTypeValid(fdpRequest)) {
				Integer isSubscriberAlreadyRegister =fnfUtil.IsSubscriberAlreadyRegisterForFaf(fdpRequest);
				if (FDPConstant.FAF_MSISDN_IS_NOT_REGISTER == isSubscriberAlreadyRegister || FDPConstant.SERVICE_CLASS_IS_NOT_ELIGIBLE == isSubscriberAlreadyRegister
						|| FDPConstant.FAF_SC_BO_NOT_FOUND == isSubscriberAlreadyRegister) {
					LOGGER.error("Subscriber is not Registered for FAF." + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ this.getClass());
					fdpResponse = new FDPResponseImpl(Status.FAILURE, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_TERMINATE));
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
		return "Subscriber is not registered for FAF.";
	}
}
