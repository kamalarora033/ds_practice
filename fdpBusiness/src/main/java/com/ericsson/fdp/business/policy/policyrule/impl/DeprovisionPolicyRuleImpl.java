package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

public class DeprovisionPolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = -61415883516779L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		final String notificationText = getNotificationText(fdpRequest);
		if (notificationText != null) {
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_CONTINUE));
		}
		return fdpResponse;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		
		String resultString = null;
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest, AuxRequestParam.CONFIRMATION.getName());
		if ((input == null && getNotificationText(fdpRequest) != null)) {
			resultString = "Cannot validate policy value";
		} else if (input != null) {
			final String validResponsesForProductBuy = FDPConstant.VALID_RESPONSE;
			boolean isValid = false;
			if (validResponsesForProductBuy != null && !validResponsesForProductBuy.isEmpty()) {
				final String[] validResponses = validResponsesForProductBuy.split(FDPConstant.COMMA);
				for (final String validResponse : validResponses) {
					if (validResponse.equalsIgnoreCase(input.toString())) {
						isValid = true;
						break;
					}
				}
			}
			if(! isValid)
				resultString ="Invalid input";
			
			if(resultString != null)
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, resultString, null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), resultString,
							TLVOptions.SESSION_TERMINATE));
		}
		return response;
	}
	
	/*
	 * Gets the policy text to show from configuration key Deprovision
	 * 
	 * @param fdpRequest
	 */
	public String getNotificationText(FDPRequest fdpRequest){
		 final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		 String notificationText = configurationMap.get(ConfigurationKey.DEPROVISION_MENU.getAttributeName());
		 if (notificationText == null || notificationText.isEmpty()) {
			 notificationText = "Press 1 for Adhoc, 2 for Recurring";
		 }
		 return notificationText;
	}

}
