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
import com.ericsson.fdp.business.util.FnfUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * Feature : Add Friends and Family Policy for Local/International number input.
 * 
 * @author evasaty
 * 
 */
public class AddFnFNumberInputPolicyRuleImpl extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = 1L;

	
	
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AddFnFNumberInputPolicyRuleImpl.class);

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
				
				if(PolicyRuleValidateImpl.isNullorEmpty(input)){
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, "Kindly provide some input."), null,
							true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
									PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, "Kindly provide some input."), TLVOptions.SESSION_TERMINATE));
					return response;
				}
				((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD,
						input.toString());
				FnfUtil fnfUtil = new FnfUtil();
				if (!FnfUtil.ValidateFafMsisdn(input.toString())
						&& validateInput(
								(Boolean) ((FDPRequestImpl) fdpRequest)
										.getAuxiliaryRequestParameter(AuxRequestParam.IS_LOCAL_ADD_FNF),
								input.toString())) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, "Input number is incorrect.", null,
							true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
									"Input number is incorrect.", TLVOptions.SESSION_TERMINATE));
				} else if (fnfUtil.isAddMoreFafNumberToAddUSSD(fdpRequest, input.toString()) != FDPConstant.FAF_MAX_ADD_TRUE) {
					Integer isAddFafNumber = fnfUtil.isAddMoreFafNumberToAddUSSD(fdpRequest, input.toString());
					String notificationText = getFAfFailureMsg(isAddFafNumber,fdpRequest);
					
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, "Input number is incorrect.", null,
							true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
									notificationText, TLVOptions.SESSION_TERMINATE));
				} else {
					/*((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER,
							input.toString());*/
					((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER,
							null);
					((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
							AuxRequestParam.FAF_EXECUTE_COMMAND_AGAIN, "true");
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
			notificationText = configurationMap.get(ConfigurationKey.FAF_MAX_ONNET.getAttributeName());
			if(null == notificationText){
				notificationText = "Maximum onnet limit reached";
			}
		}else if(isAddFafNumber == 3){
			notificationText = configurationMap.get(ConfigurationKey.FAF_MAX_OFFNET.getAttributeName());
			if(null == notificationText){
				notificationText = "Maximum offnet limit reached";
			}
		}else if(isAddFafNumber == 4){
			notificationText = configurationMap.get(ConfigurationKey.FAF_MAX_INTERNATIONAL.getAttributeName());
			if(null == notificationText){
				notificationText = "Maximum international limit reached";
			}
		}else{
			notificationText = "You cannot add faf msisdn now.";
		}
		return notificationText;
	}

	/**
	 * validate the input with international prefix.
	 * 
	 * @param isLocal
	 * @param msisdn
	 * @return
	 */
	private boolean validateInput(Boolean isLocal, String msisdn) {
		Boolean internationalPrefix = msisdn.startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX);
		if (!isLocal && !internationalPrefix) {
			return false;
		} else if (isLocal && internationalPrefix) {
			return false;
		} else
			return true;
	}

	/**
	 * Gets the policy text to show from configuration key Friends and Family
	 * add menu
	 * 
	 * @param fdpRequest
	 */
	public String getNotificationText(FDPRequest fdpRequest) {
		String notificationText = null;
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		final Boolean isLocal = (Boolean) ((FDPRequestImpl) fdpRequest)
				.getAuxiliaryRequestParameter(AuxRequestParam.IS_LOCAL_ADD_FNF);
		if (isLocal) {
			notificationText = configurationMap.get(ConfigurationKey.ADD_FNF_LOCAL_NUMBER_INPUT.getAttributeName());
		} else {
			notificationText = configurationMap.get(ConfigurationKey.ADD_FNF_INTERNATIONAL_NUMBER_INPUT
					.getAttributeName());
		}
		if (notificationText == null && isLocal) {
			notificationText = "Kindly enter the Local number";
		}
		if (notificationText == null && !isLocal) {
			notificationText = "Kindly enter the International number in the format 00-CC-Number{Eg.00260-0971xxx}";
		}
		return notificationText;
	}
}
