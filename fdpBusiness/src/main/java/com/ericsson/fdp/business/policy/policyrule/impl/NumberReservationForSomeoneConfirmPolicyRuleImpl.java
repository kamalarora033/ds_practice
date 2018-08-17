package com.ericsson.fdp.business.policy.policyrule.impl;
/**
 * This class implements the policy rule and validates the by for other
 * number confirmation policy rule for Number Reservation.
 * Execute next rule in policy rule chain. 
 * 
 */

import com.ericsson.fdp.business.enums.AuxRequestParam;
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
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

public class NumberReservationForSomeoneConfirmPolicyRuleImpl extends PolicyRuleImpl{

	private static final long serialVersionUID = 1L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		if( fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT)== null)
			return null;
		if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT) instanceof Product && (RequestUtil.isProductSpTypeValid(fdpRequest,FDPServiceProvSubType.NUMBER_RESERVATION))) {
					return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_NUMBER_TO_RESERVED_CONFIRM,"Kindly Confirm number to be reserved"),
							TLVOptions.SESSION_CONTINUE));
			}
		return null;
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)	throws ExecutionFailedException {
		
		if (null == input || null != condition)
			return new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		
		String numberForReservation = input.toString();
		if(input.toString().trim().isEmpty() || !RequestUtil.isBeneficiaryMsisdnValid(numberForReservation,fdpRequest))
			return  new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, PolicyValidationMessageEnum.SUGGESTION_MSG.msg()), null,
				true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
						PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, PolicyValidationMessageEnum.SUGGESTION_MSG.msg()), TLVOptions.SESSION_TERMINATE));
		
		if (RequestUtil.startsWithZeroAltMsisdn(numberForReservation).equals(((FDPRequestImpl) fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN))){
			//response = new FDPPolicyResponseImpl(PolicyStatus.GOTO_RULE, null, null, false, null);
			//response.setNextRuleIndex(NumberReservationPolicyIndex.NUMBER_RESERVATION_ALTERNATE_NUMBER_POLICY_RULE.getIndex());
			 return new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		}  
		
		return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyValidationMessageEnum.ERROR_MSG.msg(), null,
				true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
				PolicyValidationMessageEnum.ERROR_MSG.msg(), TLVOptions.SESSION_TERMINATE));
	}
}
