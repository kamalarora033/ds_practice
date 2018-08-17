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
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * 
 * @author Ericsson
 *
 */

public class Time2ShareAmtShareGHCPolicyRuleImple extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = -7257934946158859594L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
	}

	/**
	 * This method will valid the amount in GHC and convert and store the total
	 * amount to be transferred in fdpRequest
	 */
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);

		try {

			boolean isValid = PolicyRuleValidateImpl.isInteger(input);
			String failureMsg;

			if (isValid) {
				isValid = false;
				Integer amountInGHC = getTransferAmountInGHC(fdpRequest, Integer.parseInt(input.toString()));
				if (null != amountInGHC && amountInGHC >= 0) {

					setAmountToTransfer(fdpRequest, amountInGHC);
					setDAIdInRequest(fdpRequest);
					isValid = true;
				}
			}

			if (!isValid) {
				failureMsg = PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG,
						"Please provide numeric value only.");
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, failureMsg, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), failureMsg,
								TLVOptions.SESSION_TERMINATE));
			}
		} catch (Exception e) {
			throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
		}

		return response;
	}

	/**
	 * This method will return the total amount to transfer in GHC
	 * 
	 * @param fdpRequest
	 * @param amountInGHC
	 * @return
	 */
	private Integer getTotalAmtToTransfer(FDPRequest fdpRequest, Integer amountInGHC) {
		String conversionFactor = getConfigurationMapValue(fdpRequest, ConfigurationKey.GHC_CONVERSION_FACTOR);
		// If conversion ratio is not defined in configuration, then use 1 as default
		return (conversionFactor == null) ? 1 * amountInGHC : Integer.parseInt(conversionFactor) * amountInGHC;

	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}

	/**
	 * This method returns the message shown to user for select the amount in given list
	 * 
	 * @param fdpRequest
	 * @return
	 */

	@Override
	public String getNotificationText(FDPRequest fdpRequest) {
		String notificationText = getConfigurationMapValue(fdpRequest, ConfigurationKey.TIME2SHARE_AMT_TO_SHARE_TEXT2);
		String message = "\nSelect Credit to transfer\n1.GHC 5 \n2.GHC 10 \n3.GHC 15 \n";

		return (null != notificationText) ? formateNotificationText(notificationText) : message;
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

	private String formateNotificationText(final String notificationText) {
		String[] transferAmountList = notificationText.split(FDPConstant.COMMA);
		int counter = 0;
		StringBuilder formatedNotificationText = new StringBuilder("\nSelect Credit to transfer\n");
		for (String transferAmount : transferAmountList) {
			formatedNotificationText.append(++counter).append(FDPConstant.DOT).append(FDPConstant.SPACE)
					.append(transferAmount.trim()).append(FDPConstant.NEWLINE);

		}
		return formatedNotificationText.toString();
	}

	private Integer getTransferAmountInGHC(final FDPRequest fdpRequest, Integer inputKey)
			throws ExecutionFailedException {
		final String ghanaCurrency="GHC";
		String[] transferAmountList = getConfigurationMapValue(fdpRequest,
				ConfigurationKey.TIME2SHARE_AMT_TO_SHARE_TEXT2).split(FDPConstant.COMMA);
		final String splitText = null == getConfigurationMapValue(fdpRequest, ConfigurationKey.CURRENCY) ? ghanaCurrency
				: getConfigurationMapValue(fdpRequest, ConfigurationKey.CURRENCY);

		for (int i = 0; i < transferAmountList.length; i++) {
			if (transferAmountList[i].contains(splitText) && i + 1 == inputKey) {
				Integer amountTransferInGHC = Integer
						.parseInt((transferAmountList[i].trim().substring(splitText.length()).trim()).trim());
				((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.AMOUNT_TRANSFER_IN_GHC,
						amountTransferInGHC);
				return amountTransferInGHC;
			}
		}
		
		return null;

	}

	private void setDAIdInRequest(final FDPRequest fdpRequest) throws ExecutionFailedException{
		final Long fromDA = Long
				.parseLong(getConfigurationMapValue(fdpRequest, ConfigurationKey.TIME_2_SHARE_DA_ACCOUNT_FROM));
		final Long toDA = Long
				.parseLong(getConfigurationMapValue(fdpRequest, ConfigurationKey.TIME_2_SHARE_DA_ACCOUNT_TO));

		if (null != fromDA && null != toDA) {
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_FROM,
					fromDA);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_TO,
					toDA);
		}else{
			throw new ExecutionFailedException("DA id should not empty");
		}
	}
	
	private void setAmountToTransfer(final FDPRequest fdpRequest, final Integer amountInGHC)
			throws ExecutionFailedException {
		Integer amountToTrans = getTotalAmtToTransfer(fdpRequest, amountInGHC);

		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER, amountToTrans);
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_BE_RECIEVED,
				amountToTrans);
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.IS_TIME2SHARE, true);
	}
}
