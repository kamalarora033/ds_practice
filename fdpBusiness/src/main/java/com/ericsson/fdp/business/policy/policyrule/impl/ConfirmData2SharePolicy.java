package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.BaseProduct;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

public class ConfirmData2SharePolicy extends AbstractPolicyRule implements PolicyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 614158835797L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		String transCharges = "0";
		Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		BaseProduct baseProduct = (BaseProduct) product;
		if(baseProduct.getCharges() != null)
			transCharges	=	baseProduct.getCharges();
		final String notificationText = "You will be charged with "+transCharges+" for sharing the data. Press 1 to proceed.";
		if (notificationText != null) {
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_CONTINUE));
		}
		((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.isData2Share, FDPConstant.TRUE);
		return fdpResponse;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		
		String requestString = ((FDPSMPPRequestImpl)fdpRequest).getRequestString();
		if(null==requestString || !requestString.equals("1"))
			response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Abort data to share action.",
							TLVOptions.SESSION_TERMINATE));
		
		return response;
	}
	
}
