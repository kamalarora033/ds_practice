package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * @author ESIASAN
 *
 */
public class Time2ShareConfirmationPolicyRuleImpl extends ProductPolicyRuleImpl{
			
	private static final long serialVersionUID = -8874995713349579913L;
	
	
	

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response=new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		}else{
			try {
				
				boolean isValid =PolicyRuleValidateImpl.isNullorEmpty(input);
				if (isValid) {
					isValid = false;
					String validConfirmationInputs = getConfigurationMapValue(fdpRequest,
							ConfigurationKey.ME2U_CONFIRMATION_VALID_INPUTS);
					validConfirmationInputs = (null != validConfirmationInputs && !validConfirmationInputs.isEmpty()) ? validConfirmationInputs
							: "1";
					String[] validInputs = validConfirmationInputs.split(",");
					for (int i = 0; i < validInputs.length; i++) {
						if (validInputs[i].trim().contentEquals(input.toString())) {
							isValid = true;
							break;
						}
					}
				}else{
					isValid = false;
				}
				
				if (!isValid) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE,
							PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
									PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
									TLVOptions.SESSION_TERMINATE));
				}
				
				String validCancellationInputs = getConfigurationMapValue(fdpRequest,
						ConfigurationKey.TIME2SHARE_CANCELLATION_VALID_INPUT);
				validCancellationInputs = (null != validCancellationInputs && !validCancellationInputs.isEmpty()) ? validCancellationInputs
						: "2";
				if (input != null && validCancellationInputs.contentEquals(input.toString())) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE,
							PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.CANCEL_NOTIFICATION, null),
							null, true,
							ResponseUtil.createResponseMessageInList(
									fdpRequest.getChannel(), PolicyRuleValidateImpl
											.errorMsg(PolicyValidationMessageEnum.CANCEL_NOTIFICATION, null),
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final Exception e) {
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	@Override
	public String getNotificationText(FDPRequest fdpRequest){
		String staticMsg="Please Confirm Transfer of GHC ${transferAmount}, 1. Confirm, 2. Cancel";
		 String notificationText =   RequestUtil.getNotificationText(fdpRequest, ConfigurationKey.TIME_2_SHARE_CONFIRMATION_NOTIFICATION, staticMsg);
		 
		 if (!(notificationText == null || notificationText.isEmpty())) {
			 Object amtToShareObj = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.AMOUNT_TRANSFER_IN_GHC);
			 notificationText = notificationText.replace("${transferAmount}", amtToShareObj.toString());
			 
			 /*notificationText = notificationText.replace("${Me2uAmtToShareInKwacha}",  Me2uUtil.getAmtNgweeTokwacha(fdpRequest, Long.parseLong(amtToShareObj.toString())).toString());
			 notificationText = notificationText.replace("${Me2uMsisdnToCredit}", fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT).toString());
			 notificationText = notificationText.replace("${Me2uTransCharges}", fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_TRANS_CHARGES).toString());*/
		 }
		 return notificationText;
	}
	
	/**
	 * This method will return the value of input configuration key as defined in fdpCircle
	 * @return
	 */
	private String getConfigurationMapValue(FDPRequest fdpRequest, ConfigurationKey key){
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationMap.get(key.getAttributeName());		
	}

	
}