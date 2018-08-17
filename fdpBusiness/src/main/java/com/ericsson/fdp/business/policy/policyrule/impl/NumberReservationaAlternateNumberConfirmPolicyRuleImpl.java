package com.ericsson.fdp.business.policy.policyrule.impl;
/**
 * This class implements the policy rule and validates the alternate
 * number confirmation policy rule for Number Reservation.
 * Skips next rule in policy rule chain. 
 * 
 */

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
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
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;


public class NumberReservationaAlternateNumberConfirmPolicyRuleImpl extends PolicyRuleImpl{

	private static final long serialVersionUID = 1L;
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		if(null == fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT))
			return null;
		if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT) instanceof Product && (RequestUtil.isProductSpTypeValid(fdpRequest,FDPServiceProvSubType.NUMBER_RESERVATION))) {
					return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_ALTERNATE_NUMBER_CONFIRM,"Kindly Confirm Alternate number"),
							TLVOptions.SESSION_CONTINUE));
			}
		return null;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)	throws ExecutionFailedException {
		
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		
		if (null == input || null != condition)
			return new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		
		String alternateNumberOrEmailId = input.toString();
		
		if (alternateNumberOrEmailId.matches("\\d+")) {
			if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NUMBER_RESERVATION_ALTERNATE_NUMBER) != null) {
				if(input.toString().trim().isEmpty() || !RequestUtil.isBeneficiaryMsisdnValid(alternateNumberOrEmailId,fdpRequest)) {
					String failureMsg = RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.ALTERNATE_NUMBER_CONFORM_INVALID_TEXT,"Confirm Alternate Number is not Valid.");
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, failureMsg, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), failureMsg,
									TLVOptions.SESSION_TERMINATE));
				}		
				
				if (RequestUtil.startsWithZeroAltMsisdn(alternateNumberOrEmailId).equals((String) ((FDPRequestImpl) fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.NUMBER_RESERVATION_ALTERNATE_NUMBER))) {
					return response;
				}
								 
				return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyValidationMessageEnum.ERROR_MSG.msg(), null,	true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
								 PolicyValidationMessageEnum.ERROR_MSG.msg(), TLVOptions.SESSION_TERMINATE));
			} else {
				String failureMsg = RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.ALTERNATE_EMAIL_ID_CONFORM_INVALID_TEXT," Confirm Email Id is not valid.");
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, failureMsg, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), failureMsg,
								TLVOptions.SESSION_TERMINATE));
			}
			
		} else {
			if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.EMAIL_ID) != null) {
				if (!alternateNumberOrEmailId.equalsIgnoreCase((String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.EMAIL_ID))) {
					String failureMsg = RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.ALTERNATE_EMAIL_ID_CONFORM_INVALID_TEXT," Confirm Email Id is not valid.");
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, failureMsg, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), failureMsg,
									TLVOptions.SESSION_TERMINATE));
				}
			} else {
				String failureMsg = RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.ALTERNATE_NUMBER_CONFORM_INVALID_TEXT,"Confirm Alternate Number is not Valid.");
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, failureMsg, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), failureMsg,
								TLVOptions.SESSION_TERMINATE));
			}
		}
		
		return response;
	}
	
}
