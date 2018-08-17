package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

public class SubSharedDataPolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 61415883516997L;

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

	private String getNotificationText(FDPRequest fdpRequest) {
		 final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		 String notificationText = configurationMap.get(ConfigurationKey.DATA2SHARE_AMOUNT_NOTIF.getAttributeName());
		 Me2uProductDTO me2uProductDTO = (Me2uProductDTO) ((FDPSMPPRequestImpl)fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT);
			String availableData = me2uProductDTO.getAvailableBalance();
		 if (notificationText == null || notificationText.isEmpty()) {
			 notificationText = "Enter the Data Volume to be shared (in MB) (Without decimal places, the Max value that can be shared is "+availableData+") ";
		 }
		 else {
			 if(notificationText.contains("${dataAmount}")) {
				 notificationText = notificationText.replace("${dataAmount}", availableData);
			 }
		 }
		 return notificationText;
	}

	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
					throws ExecutionFailedException {

		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		String requestString = ((FDPSMPPRequestImpl)fdpRequest).getRequestString();
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		try {
			Boolean dataAmtCheck = requestString.matches("[0-9]+");
			Long notificationId = Long.valueOf(BusinessConstants.MAX_DATA_CONSTRAINT);
			Long InvalidDataNotificationId = Long.valueOf(BusinessConstants.INVALID_DATA_AMOUNT);
			Me2uProductDTO me2uProductDTO = (Me2uProductDTO) ((FDPSMPPRequestImpl)fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.SELECTED_Me2UDATA2SHARE_PRODUCT);
			String availableData = me2uProductDTO.getAvailableBalance();
			if(!dataAmtCheck) {
				return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), NotificationUtil.createNotificationText(fdpRequest, InvalidDataNotificationId, circleLogger),
								TLVOptions.SESSION_TERMINATE));
			}
			if(null==requestString || Long.valueOf(requestString) > Long.valueOf(availableData)) {
				return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger),
								TLVOptions.SESSION_TERMINATE));
			}
			
			
			((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER, Long.valueOf(requestString));
		}
		catch(Exception e) {
			response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), requestString,
							TLVOptions.SESSION_TERMINATE));
		}

		return response;
	}

}
