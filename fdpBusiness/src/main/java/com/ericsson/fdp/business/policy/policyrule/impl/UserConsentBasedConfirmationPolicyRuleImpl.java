package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * @author EAMASAC
 *
 */
public class UserConsentBasedConfirmationPolicyRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = -8636438791614204867L;
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		return new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, false, null);
	}



	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	protected String getNotificationText(FDPRequest fdpRequest) {
		FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		String notificationText = null;
		if (fdpCacheable instanceof Product) {
			Product product = (Product) fdpCacheable;
			notificationText = product.getAdditionalInfo(ProductAdditionalInfoEnum.IS_REFUND);
		}
		return notificationText;
	}
}