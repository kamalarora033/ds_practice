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

/**
 * Policy to get delete Faf number confirmation.
 * 
 * @author evasaty
 * 
 */
public class DeleteFnFConfirmationPolicyRuleImpl extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(DeleteFnFConfirmationPolicyRuleImpl.class); 
	
	
	

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
		return fdpResponse;
	}

	public String getNotificationText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		StringBuffer notificationText = new StringBuffer();
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		notificationText.append(configurationMap.get(ConfigurationKey.YOU_HAVE_SELECTED.getAttributeName()));
		notificationText.append(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER)
				+ FDPConstant.PARAMETER_SEPARATOR);
		notificationText.append(configurationMap.get(ConfigurationKey.TO_DELETE.getAttributeName()));
		return notificationText.append(configurationMap.get(ConfigurationKey.PRESS_1_CONFIRMATION_TEXT.getAttributeName())).toString();
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
				if (isValid) {
					isValid = false;
					final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
					final String validResponsesForDeleteConfirmation = configurationMap
							.get(ConfigurationKey.PRESS_1_CONFIRMATION.getAttributeName());
					if (validResponsesForDeleteConfirmation != null && !validResponsesForDeleteConfirmation.isEmpty()) {
						final String[] validResponses = validResponsesForDeleteConfirmation.split(FDPConstant.COMMA);
						for (final String validResponse : validResponses) {
							if (validResponse.equalsIgnoreCase(input.toString())) {
								isValid = true;
								break;
							}
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
