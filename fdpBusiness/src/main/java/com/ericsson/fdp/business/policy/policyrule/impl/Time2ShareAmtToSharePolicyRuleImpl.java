package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.Me2uUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;


/**
 * This policy will ask the amount(in Kwacha) that the subscriber wants to share to other subscriber.
 * @author ESIASAN
 *
 */
public class Time2ShareAmtToSharePolicyRuleImpl extends ProductPolicyRuleImpl{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2057902198369845783L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false,
				ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		} else {
			try {
				boolean isValid = input != null ? StringUtil.isStringDoubleType(input.toString()) : false;
				if (isValid) {
					Double amtInKwacha = Double.parseDouble(input.toString());
					amtInKwacha = Math.round(amtInKwacha * 100.0) / 100.0;
					if (amtInKwacha >= 1.0) {
						isValid = true;
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
								AuxRequestParam.ME2U_AMT_TO_TRANSFER, amtInKwacha.toString());
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
								AuxRequestParam.POLICY_RETRY_ERROR_MSG, null);
					} else
						isValid = false;
				}
				if (!isValid) {
					String errorMsg = "Amount entered is invalid. Please re-enter the numeric amount you want to share between 1 kwacha and "
							+ Me2uUtil.getOnceMaxTransLimit(fdpRequest) + " kwacha.";
					((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.POLICY_RETRY_ERROR_MSG,
							errorMsg);
					response = new FDPPolicyResponseImpl(PolicyStatus.RETRY_POLICY, errorMsg, null, false,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), errorMsg,
									TLVOptions.SESSION_CONTINUE));
				}
			} catch (Exception e) {
				throw new ExecutionFailedException(
						"The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}
	
	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	/**
	 * This method returns the message shown to user for enter the amount to share in kwacha
	 */
	public String getNotificationText(FDPRequest fdpRequest) {
		String notificationText = null;
		if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.POLICY_RETRY_ERROR_MSG)){
			notificationText = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.POLICY_RETRY_ERROR_MSG).toString();
		}else{
			notificationText = getConfigurationMapValue(fdpRequest, ConfigurationKey.TIME2SHARE_AMT_TO_SHARE_TEXT1);
		}
		return (null != notificationText) ? notificationText : "Enter amount in kwacha:";
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