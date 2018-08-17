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
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

public class ModifyFnFAddNumberPolicyRuleImpl extends PolicyRuleImpl {

	private static final long serialVersionUID = 1L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ModifyFnFAddNumberPolicyRuleImpl.class);
	
	
	

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
		return fdpResponse;
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
					isValid = false;
					final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
					final String validResponsesAddLocalChoice = configurationMap
							.get(ConfigurationKey.ADD_FAF_LOCAL_VALID_RESPONSES.getAttributeName());
					final String validResponsesAddInternationalChoice = configurationMap
							.get(ConfigurationKey.ADD_FAF_INTERNATIONAL_VALID_RESPONSES.getAttributeName());
					if (validResponsesAddLocalChoice != null && !validResponsesAddLocalChoice.isEmpty()) {
						if (validResponsesAddLocalChoice.equalsIgnoreCase(input.toString())) {
							isValid = true;
							((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.IS_LOCAL_ADD_FNF,
									Boolean.TRUE);
						}
					}
					if (validResponsesAddInternationalChoice != null && !validResponsesAddInternationalChoice.isEmpty()) {
						if (validResponsesAddInternationalChoice.equalsIgnoreCase(input.toString())) {
							isValid = true;
							((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.IS_LOCAL_ADD_FNF,
									Boolean.FALSE);
						}
					}
				}
				
				if (!isValid) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final Exception e) {
				LOGGER.error("Policy rule could not be evaluated" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ this.getClass());
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}

	/**
	 * Gets the policy text to show from configuration key Friends and Family
	 * add menu
	 * 
	 * @param fdpRequest
	 */
	public String getNotificationText(FDPRequest fdpRequest) {
		StringBuffer notificationText = new StringBuffer();
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String notification = configurationMap.get(ConfigurationKey.ADD_FNF_DISPLAY_TEXT.getAttributeName());
		notificationText.append(configurationMap.get(ConfigurationKey.YOU_HAVE_SELECTED.getAttributeName())
				+ ((FDPRequestImpl) fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE)
						.toString() + configurationMap.get(ConfigurationKey.TO_MODIFY.getAttributeName()) + FDPConstant.PARAMETER_SEPARATOR);
		if (notification == null || notification.isEmpty()) {
			notificationText.append("Select option to add Under Friends and Family. 1 Local[Onnet] 2.International");
		} else {
			notificationText.append(notification);
		}
		return notificationText.toString();
	}
}
