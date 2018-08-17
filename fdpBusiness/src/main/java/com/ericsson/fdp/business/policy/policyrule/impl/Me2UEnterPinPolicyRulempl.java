package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * This class is used the take user input for Me2U Pin and Validate it
 * @author GUR21122
 * @version 1.0
 * @since 2017-10-15
 *
 */
public class Me2UEnterPinPolicyRulempl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = 1L;
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		} else {
			try {
				
				if (!PolicyRuleValidateImpl.isNullorEmpty(input) || !isPinValid(fdpRequest, input.toString())) {
					String errorMsg = getErrorNotification(fdpRequest);
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, errorMsg, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), errorMsg,
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final Exception e) {
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}

	/**
	 * This method will validate the input pin
	 * 
	 * @param pin
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean isPinValid(FDPRequest fdpRequest, String pin) throws ExecutionFailedException {
		Boolean isPinValid = false;
		
		if (StringUtil.isStringIntegerType(pin)) {
			int pinLength = getConfigurationMapValue(fdpRequest,ConfigurationKey.ME2U_PIN_LENGTH) == null ? 
					4 : Integer.parseInt(getConfigurationMapValue(fdpRequest,ConfigurationKey.ME2U_PIN_LENGTH));
			
			if (pin.length() == pinLength) {
				isPinValid = true;
				((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ME2U_PIN, pin);
			}
				
		}
		
		return isPinValid;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}

	@Override
	public String getNotificationText(FDPRequest fdpRequest) {
		String notificationText = getConfigurationMapValue(fdpRequest,
				ConfigurationKey.ME2U_ENTER_PIN_TEXT);
		if (notificationText == null || notificationText.isEmpty()) {
			notificationText = "Please Enter your PIN";
		}
		return notificationText;
	}

	/**
	 * This method will return the value of input configuration key as defined
	 * in fdpCircle
	 * 
	 * @return
	 */
	private String getConfigurationMapValue(FDPRequest fdpRequest, ConfigurationKey key) {
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationMap.get(key.getAttributeName());
	}
	
	/**
	 * This method will return error notification text
	 * @param fdpRequest
	 * @return
	 */
	private String getErrorNotification(FDPRequest fdpRequest) {
		String notificationText = getConfigurationMapValue(fdpRequest,
				ConfigurationKey.ME2U_PIN_INVALID_TEXT);
		if (notificationText == null || notificationText.isEmpty()) {
			notificationText = "Please Enter a valid PIN";
		}
		return notificationText;
	}

}