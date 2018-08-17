package com.ericsson.fdp.business.policy.policyrule.impl;
/**
 * This class implements the policy rule and validates the alternate
 * number policy rule for Number Reservation.
 * Based on user input execute next rule in policy rule chain. 
 * 
 */

import org.apache.commons.validator.routines.EmailValidator;

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

public class NumberReservationAlternateNumberPolicyRuleImpl extends PolicyRuleImpl{

	private static final long serialVersionUID = 1L;
	EmailValidator emailValidator = null;
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		if(null == fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT))
			return null;
		if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT) instanceof Product && (RequestUtil.isProductSpTypeValid(fdpRequest,FDPServiceProvSubType.NUMBER_RESERVATION))) {
				return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_ALTERNATE_NUMBER,"Provide an alternate number"),
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
			
			String msisdn  = String.valueOf(fdpRequest.getSubscriberNumber());
			
			if(alternateNumberOrEmailId.trim().isEmpty() || !RequestUtil.isBeneficiaryMsisdnValid(alternateNumberOrEmailId,fdpRequest))
				return  new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, PolicyValidationMessageEnum.SUGGESTION_MSG.msg()), null,
					true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
								PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, PolicyValidationMessageEnum.SUGGESTION_MSG.msg()), TLVOptions.SESSION_TERMINATE));
			
			alternateNumberOrEmailId = RequestUtil.startsWithZeroAltMsisdn(alternateNumberOrEmailId);
			
			if (null !=((FDPRequestImpl) fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN) && alternateNumberOrEmailId.equalsIgnoreCase((String)((FDPRequestImpl) fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN)))
				return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, PolicyValidationMessageEnum.SUGGESTION_MSG.msg()), null,
					  true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
						PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_NUMBER_TO_RESERVED_SUGGESTION,"Beneficiary subcriber's number and alternate number cannot be same.")), TLVOptions.SESSION_TERMINATE));
			
			if((null ==((FDPRequestImpl) fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN)) && alternateNumberOrEmailId.equalsIgnoreCase(msisdn))
				return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, PolicyValidationMessageEnum.SUGGESTION_MSG.msg()), null,
				  true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
						PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.NUMBER_RESERVATION_ALTERNATE_NUMBER_SUGGESTION,"Beneficiary number and alternate number cannot be same.")), TLVOptions.SESSION_TERMINATE));	
			
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.NUMBER_RESERVATION_ALTERNATE_NUMBER, alternateNumberOrEmailId);
			
		} else {
			
			emailValidator = EmailValidator.getInstance();
			if (emailValidator.isValid(alternateNumberOrEmailId)) {
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.EMAIL_ID, alternateNumberOrEmailId);
				
			} else {
				String failureMsg = RequestUtil.getNotificationText(fdpRequest,ConfigurationKey.INVALID_EMAIL_ID_TEXT,"Email Id is not valid.");
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, failureMsg, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), failureMsg,
								TLVOptions.SESSION_TERMINATE));	
			}
			
		}
		
		//FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.GOTO_RULE, null, null, false, null);
		//putValueInAltNumbAuxParamNextRule(response,fdpRequest,AuxRequestParam.NUMBER_RESERVATION_ALTERNATE_NUMBER, NumberReservationPolicyIndex.NUMBER_RESERVATION_ALTERNATE_NUMBER_CONFIRM_POLICY_RULE,alternateNumber);
		return response;
	}
	
	/**
	 * Puts the alternate number in Auxiliary request Parameter Map.
	 * @param fdpPolicyResponse is policy response
	 * @param fdpRequest is the fdp request
	 * @param auxRequestParam is the Auxiliary Parameter name 
	 * @return 
	 *//*
	private void putValueInAltNumbAuxParamNextRule(FDPPolicyResponse fdpPolicyResponse,FDPRequest fdpRequest,AuxRequestParam auxRequestParam,NumberReservationPolicyIndex index,String altnum){
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(auxRequestParam, altnum);
		fdpPolicyResponse.setNextRuleIndex(index.getIndex());
	}*/
}

