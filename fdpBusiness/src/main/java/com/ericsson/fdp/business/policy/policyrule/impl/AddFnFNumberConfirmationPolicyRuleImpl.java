package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.TLVOptions;

import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * Policy to get confirmation for addition to friends and family number.
 * 
 * @author evasaty
 * 
 */
public class AddFnFNumberConfirmationPolicyRuleImpl extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ModifyFnFGetFnFListPolicyRuleImpl.class);

	
	
	
	/**
	 * Gets the policy text to show from configuration key Friends and Family
	 * Add menu
	 * 
	 * @param fdpRequest
	 */
	@Override
	public String getNotificationText(FDPRequest fdpRequest) {
		String notification;
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		notification = configurationMap.get(ConfigurationKey.ADD_FNF_CONFIRMATION_TEXT.getAttributeName());
		if (null != notification) {
			if ((Boolean) ((FDPRequestImpl) fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.IS_LOCAL_ADD_FNF) && notification.contains("*")) {
				notification = notification.replace("*",
						configurationMap.get(ConfigurationKey.AMOUNT_FAF_ADD_LOCAL.getAttributeName()));
			} else if(notification.contains("*")) {
				notification = notification.replace("*",
						configurationMap.get(ConfigurationKey.AMOUNT_FAF_ADD_INTERNATIONAL.getAttributeName()));
			} else {
				return notification;
			}
		} else {
			notification = "You'll be charged with 0 Kwacha. Press 1 to confirm your additional of Friends and Family number.";
		}
		return notification;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		} else {
			try {
				
				Boolean isValid = PolicyRuleValidateImpl.isNullorEmpty(input);
				if(isValid){
					final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
					final String validChoice = configurationMap.get(ConfigurationKey.PRESS_1_CONFIRMATION.getAttributeName());
					isValid = false;
					if (validChoice != null && !validChoice.isEmpty()) {
						if (validChoice.equalsIgnoreCase(input.toString())) {
							isValid = true;
						}
					}
				}
				
				if (!isValid) {
					LOGGER.error("Input is incorrect.");
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final Exception e) {
				LOGGER.error("The policy rule could not be evaluated." + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ this.getClass());
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}
}
