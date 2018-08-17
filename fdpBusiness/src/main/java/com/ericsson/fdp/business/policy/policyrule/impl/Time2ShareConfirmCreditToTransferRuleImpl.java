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
 * @author Ericsson
 *
 */
public class Time2ShareConfirmCreditToTransferRuleImpl extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = -8874995713349579913L;

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

				boolean isValid = PolicyRuleValidateImpl.isNullorEmpty(input);
				String validMsisdn=RequestUtil.validateBeneficiaryMsisdn(fdpRequest, input.toString());
				if (isValid) {
					isValid = false;
					if (validMsisdn
							.equals(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT).toString()))
						isValid = true;
				}

				if (!isValid) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE,
							PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
									PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
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

	/**
	 * This method will return the Notification text which is configured in
	 * fdpCircle
	 * 
	 * @param fdpRequest
	 * @return
	 */
	@Override
	public String getNotificationText(FDPRequest fdpRequest) {
		String notificationText = getConfigurationMapValue(fdpRequest, ConfigurationKey.ME2U_CONFIRMATION_TEXT);
		if (notificationText == null || notificationText.isEmpty()) {
			notificationText = "Confirm Number to Transfer Credit to";
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

}