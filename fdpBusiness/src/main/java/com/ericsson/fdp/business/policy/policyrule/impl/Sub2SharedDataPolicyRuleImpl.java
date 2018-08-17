package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;

public class Sub2SharedDataPolicyRuleImpl extends AbstractPolicyRule implements PolicyRule {

	/**
	 * 
	 */
	private static final long serialVersionUID = 61415887776997L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String notificationText = configurationMap.get(ConfigurationKey.DATA2SHARE_RECEIPIENT_NOTIF.getAttributeName());
		
		 if (notificationText == null || notificationText.isEmpty()) {
			 notificationText = "Please enter the receipient mobile number (eg 96********)";
		 }
		
		
		fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_CONTINUE));
		
		return fdpResponse;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		try {
		Long notificationId = Long.valueOf(BusinessConstants.SAME_SUBSCRIBER_NOTIFICATION);
		String requestString = ((FDPSMPPRequestImpl)fdpRequest).getRequestString();
		Long incomingSubscriber = ((FDPSMPPRequestImpl)fdpRequest).getIncomingSubscriberNumber();
		if(null==requestString) 
			return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Invalid Number",
							TLVOptions.SESSION_TERMINATE));
		
		if(!isBeneficiaryMsisdnValid(fdpRequest, input.toString())){
			return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, "Request is invalid for beneficiary number", null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Request is invalid for beneficiary number",
							TLVOptions.SESSION_TERMINATE));
		}
		String benMsisdn = ((FDPSMPPRequestImpl)fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT).toString();
		if(benMsisdn.equals(incomingSubscriber.toString()))
			return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger),
							TLVOptions.SESSION_TERMINATE));		
		
		} catch (NotificationFailedException e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * This method will validate the input beneficiary number on AIR interface and do number series check
	 * @param beneficiaryMsisdn
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private boolean isBeneficiaryMsisdnValid(FDPRequest fdpRequest, String beneficiaryMsisdn) throws ExecutionFailedException {
		Boolean isBeneficiaryMsisdnValid = false;
		try {
			FDPCircle benMsisdnCircle = CircleCodeFinder.getFDPCircleByMsisdn(beneficiaryMsisdn, ApplicationConfigUtil.getApplicationConfigCache());
			if(null != benMsisdnCircle && benMsisdnCircle.getCircleName().contentEquals(fdpRequest.getCircle().getCircleName())){
				/** Adding country code to input msisdn if not already present **/
				final Map<String, String> configurationsMap = benMsisdnCircle.getConfigurationKeyValueMap();
				final Integer allowedLength = Integer.parseInt(configurationsMap.get(ConfigurationKey.MSISDN_NUMBER_LENGTH.getAttributeName()));
				if (beneficiaryMsisdn.length() <= allowedLength) {
					final FDPAppBag bag = new FDPAppBag();
					bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
					bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
					final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil.getApplicationConfigCache();
					final String countryCode = (String) fdpCache.getValue(bag);
					beneficiaryMsisdn = countryCode + beneficiaryMsisdn;
				}
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, beneficiaryMsisdn);	
				ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, true, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
				CommandUtil.executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE, true);
				ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, false, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, null);
				FDPCommand fdpCommand = fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName());
				if(!fdpCommand.getResponseError().getResponseCode().contentEquals("102")){
					isBeneficiaryMsisdnValid = true;
					((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT, beneficiaryMsisdn);
				}
			}else{
				FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(), "isBeneficiaryMsisdnValid", 
						"Request is invalid for beneficiary msisdn : " + beneficiaryMsisdn);
			}
		}
		catch (Exception e) {}
		return isBeneficiaryMsisdnValid;
	}

}
