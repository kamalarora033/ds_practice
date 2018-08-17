package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
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
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

public class OptInOptOutDCPolicyRuleImpl extends ProductPolicyRuleImpl{

	/**
	 * Class serial version UID.
	 */
	private static final long serialVersionUID = 61415883516799L;


/*	protected String getNotificationText(final FDPRequest fdpRequest) {
		return "Press 1 to confirm";
	}*/

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), policyRuleText, TLVOptions.SESSION_CONTINUE));
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.EXECUTE_SP, null, null, true, null);
		
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest, AuxRequestParam.CONFIRMATION.getName());
		if ((input == null && policyRuleText == null)) {
			throw new ExecutionFailedException("Cannot validate policy value");
		} else if (input != null) {
			final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
			final String validResponsesForProductBuy = configurationMap
					.get(ConfigurationKey.PRODUCT_BUY_VALID_RESPONSES.getAttributeName());
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
			if (!isValid) {
				response = getResponseForInvalidInput(input, fdpRequest, configurationMap);
			}
			if(null!= otherParams[2]){
				String currentNodeText = otherParams[2].toString();
				if(isValid && currentNodeText.equalsIgnoreCase("All")){
					((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_NODE, FDPConstant.OPTIN_OPTOUT_NODE_TYPE);
				}
			}
		}
		return response;
	}
}
