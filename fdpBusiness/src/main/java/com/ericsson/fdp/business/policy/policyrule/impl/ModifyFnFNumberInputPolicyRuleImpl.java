package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.FnfUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

public class ModifyFnFNumberInputPolicyRuleImpl extends PolicyRuleImpl {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ModifyFnFNumberInputPolicyRuleImpl.class);
	
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		AddFnFNumberInputPolicyRuleImpl addFnFNumberInputPolicyRuleImpl = new AddFnFNumberInputPolicyRuleImpl();
		fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), addFnFNumberInputPolicyRuleImpl.getNotificationText(fdpRequest),
				TLVOptions.SESSION_CONTINUE));
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
				
				FnfUtil fnfUtil = new FnfUtil();
				if (input==null || !FnfUtil.ValidateFafMsisdn(input.toString())) {
					LOGGER.error("Invalid Msisdn to modify. INPUT" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
							+ input);
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, "Input number is incorrect.", null,
							true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
									"Input number is incorrect.", TLVOptions.SESSION_TERMINATE));
					return response;
				}
				String fafMsisdnDelete = ((FDPRequestImpl) fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE).toString();
				((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD,input);
				//else if (!fnfUtil.isAddFafNumberAllowed(fdpRequest, input.toString())) {  
				if(fnfUtil.isAddMoreFafNumberToModify(fdpRequest) != 1){
					
					Integer isAddFafNumber = fnfUtil.isAddMoreFafNumberToAddUSSD(fdpRequest, input.toString());
					String notificationText = getFAfFailureMsg(isAddFafNumber,fdpRequest);
					
					LOGGER.error("Maximum faf add limit reached for Input Msisdn "
							+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + input.toString());
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE,
							"Maximum faf add limit reached for Input Msisdn", null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
									"You cannot modify this number. Maximum FAF limit reached",
									TLVOptions.SESSION_TERMINATE));
				} else {
					((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD, input.toString());
					((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER, input.toString());
				}
			} catch (final Exception e) {
				LOGGER.error("The policy rule could not be evaluated." + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ this.getClass());
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}
	
	private String getFAfFailureMsg(Integer isAddFafNumber, FDPRequest fdpRequest) {
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String notificationText;
		if(isAddFafNumber == 2){
			notificationText = configurationMap.get(ConfigurationKey.FAF_MODIFY_MAX_ONNET.getAttributeName());
			if(null == notificationText){
				notificationText = "Maximum onnet limit reached";
			}
		}else if(isAddFafNumber == 3){
			notificationText = configurationMap.get(ConfigurationKey.FAF_MODIFY_MAX_OFFNET.getAttributeName());
			if(null == notificationText){
				notificationText = "Maximum offnet limit reached";
			}
		}else if(isAddFafNumber == 4){
			notificationText = configurationMap.get(ConfigurationKey.FAF_MODIFY_MAX_INTERNATIONAL.getAttributeName());
			if(null == notificationText){
				notificationText = "Maximum international limit reached";
			}
		}else{
			notificationText = "You cannot modify faf msisdn now.";
		}
		return notificationText;
	}

}