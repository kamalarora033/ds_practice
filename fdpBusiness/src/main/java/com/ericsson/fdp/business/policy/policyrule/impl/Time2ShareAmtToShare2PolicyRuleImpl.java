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
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.Me2uUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.TariffEnquiryNotificationUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;


/**
 * This policy will ask the amount(in Ngwee) that the subscriber wants to share to other subscriber.
 * @author ESIASAN
 *
 */
public class Time2ShareAmtToShare2PolicyRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = -7257934946158859594L;
	

	

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false,
				ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
	}

	/**
	 * This method will valid the amount in Ngwee and convert and store the total amount to be transferred
	 * in fdpRequest
	 */
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);

		try {
            
			boolean isValid = PolicyRuleValidateImpl.isDouble(input);

			String failureMsg = null;
			if (isValid) {
				isValid = false;
				Long amtInNgwee = Math.round(Double.parseDouble(input.toString()));
				;
				if (amtInNgwee >= 0) {
					Long amtToTrans = getTotalAmtToTransfer(fdpRequest, amtInNgwee);
					Me2uUtil.getTransCharges(fdpRequest, amtToTrans);
					if (Me2uUtil.areTransAmtLimitsValid(fdpRequest, amtToTrans)) {
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
								AuxRequestParam.ME2U_AMT_TO_TRANSFER, amtToTrans);
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
								AuxRequestParam.ME2U_AMT_TO_BE_RECIEVED, amtToTrans);
						isValid = true;
					} else {
						failureMsg = TariffEnquiryNotificationUtil.createNotificationText(
								fdpRequest,
								Long.valueOf(fdpRequest.getAuxiliaryRequestParameter(
										AuxRequestParam.NOTIFICATION_TO_OVERRIDE).toString()),
								LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
					}
				}
			}

			if (!isValid) {

				if (failureMsg == null) {
					failureMsg = PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, "Please provide numeric value only.");
				}

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
	 * This method will return the total amount to transfer in Kwacha
	 * @param fdpRequest
	 * @param amtInNgwee
	 * @return
	 */
	private Long getTotalAmtToTransfer(FDPRequest fdpRequest, Long amtInNgwee) {
		Long totalAmtToTransfer = 0L;
		Object prevAmtToTransInKwachaObj = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
		if(null != prevAmtToTransInKwachaObj){
			Long ngweesInOneKwacha = Me2uUtil.getNgweeInOneKwachaa(fdpRequest);
			// Calculate the total amount to transfer in Ngwee
			totalAmtToTransfer = Math.round((Double.parseDouble(prevAmtToTransInKwachaObj.toString()) * ngweesInOneKwacha) + amtInNgwee);
		}
		return totalAmtToTransfer;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	/**
	 * This method returns the message shown to user for enter the amount to share in Ngwee
	 */
	public String getNotificationText(FDPRequest fdpRequest) {
		String notificationText = getConfigurationMapValue(fdpRequest, ConfigurationKey.TIME2SHARE_AMT_TO_SHARE_TEXT2);
		return (null != notificationText) ? notificationText : "Enter amount in Ngwee:";
	}
	
	/**
	 * This method will return the value of input configuration key as defined in fdpCircle
	 * @return
	 */
	private String getConfigurationMapValue(FDPRequest fdpRequest, ConfigurationKey key){
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationMap.get(key.getAttributeName());		
	}

	/**
	 * Invalid Input text.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String invalidInputText(final FDPRequest fdpRequest) {
		String responseString = fdpRequest.getCircle().getConfigurationKeyValueMap().get(ConfigurationKey.INVALID_INPUT_STRING.getAttributeName());
		if (responseString == null || responseString.isEmpty()) {
			responseString = "Input invalid";
		}
		return responseString;
	}
}