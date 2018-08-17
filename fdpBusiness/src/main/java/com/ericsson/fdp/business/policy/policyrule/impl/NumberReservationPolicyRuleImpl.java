package com.ericsson.fdp.business.policy.policyrule.impl;
/**
 * This class implements the policy rule and validates the first
 * policy rule for Number Reservation.
 * Based on user input execute next rule in policy rule chain. 
 * 
 */


import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.NumberReservationPolicyIndex;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

public class NumberReservationPolicyRuleImpl extends PolicyRuleImpl{

	private static final long serialVersionUID = 1L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		if(null == fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT))
			return null;
		if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT) instanceof Product && (RequestUtil.isProductSpTypeValid(fdpRequest,FDPServiceProvSubType.NUMBER_RESERVATION))) {
			return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), 
					RequestUtil.getNotificationText(fdpRequest, ConfigurationKey.NUMBER_RESERVATION, "Please select Your option,1. Reserve for self,2. Reserve for someone"),
						TLVOptions.SESSION_CONTINUE));
		}
		return null;
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,FDPRequest fdpRequest, Object... otherParams)throws ExecutionFailedException {
		if (null == input || null != condition)
			return new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		if(input.toString().trim().isEmpty() || RequestUtil.getConfigurationKeyValue(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_SELF).isEmpty() || RequestUtil.getConfigurationKeyValue(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_FOR_SOMEONE).isEmpty() ||
				!input.toString().matches("[1-2]"))
			return  new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, PolicyValidationMessageEnum.SUGGESTION_MSG.msg()), null,
					true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
				PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, 
				RequestUtil.getNotificationText(fdpRequest, ConfigurationKey.NUMBER_RESERVATION_ERROR_SUGGESTION, PolicyValidationMessageEnum.SUGGESTION_MSG.msg())), 
				TLVOptions.SESSION_TERMINATE));
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.GOTO_RULE, null, null, false, null);
		String value = input.toString();
		if (RequestUtil.getConfigurationKeyValue(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_SELF).equalsIgnoreCase(value)) 
			response.setNextRuleIndex(NumberReservationPolicyIndex.NUMBER_RESERVATION_ALTERNATE_NUMBER_POLICY_RULE.getIndex());	
		if (RequestUtil.getConfigurationKeyValue(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_FOR_SOMEONE).equalsIgnoreCase(value))
			response.setNextRuleIndex(NumberReservationPolicyIndex.NUMBER_RESERVATION_FOR_SOMEONE_POLICY_RULE.getIndex());
		return response;
	}
}
