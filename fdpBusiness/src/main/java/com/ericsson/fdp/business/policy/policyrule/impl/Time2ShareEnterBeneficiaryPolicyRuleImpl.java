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
import com.ericsson.fdp.business.util.Me2uUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * @author ESIASAN
 *
 */
public class Time2ShareEnterBeneficiaryPolicyRuleImpl extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = -8636438791614204867L;

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
				
				if (!PolicyRuleValidateImpl.isNullorEmpty(input)
						|| !isBeneficiaryMsisdnValid(fdpRequest, input.toString()) || Me2uUtil.isSubscriberMsisdnSameAsBeneficiary(fdpRequest, input.toString())) {
					String errorMsg = PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG,
							"Request is invalid for beneficiary number");
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
	 * This method will validate the input beneficiary number on AIR interface
	 * and do number series check
	 * 
	 * @param beneficiaryMsisdn
	 * @return
	 * @throws ExecutionFailedException
	 */
	private boolean isBeneficiaryMsisdnValid(FDPRequest fdpRequest,
			String beneficiaryMsisdn) throws ExecutionFailedException {
		Boolean isBeneficiaryMsisdnValid = false;
		Long validBeneficiaryMsisdn =Long.parseLong(RequestUtil.validateBeneficiaryMsisdn(
				fdpRequest, beneficiaryMsisdn));

		if (null != validBeneficiaryMsisdn) {
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
					AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT,
					validBeneficiaryMsisdn);
			isBeneficiaryMsisdnValid = true;
		}

		return isBeneficiaryMsisdnValid;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}

	@Override
	public String getNotificationText(FDPRequest fdpRequest) {
		String notificationText = getConfigurationMapValue(fdpRequest,
				ConfigurationKey.ME2U_ENTER_BENEFICIARY_MSISDN_TEXT);
		if (notificationText == null || notificationText.isEmpty()) {
			notificationText = "Enter Number to Transfer Credit to";
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