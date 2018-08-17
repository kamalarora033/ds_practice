package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.display.ResponseMessage;
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


/*
Feature Name: User has to specify a FaF number for availing the 'Magic Number facility on this number
Changes: New policy 'AddFnFNumberForMagicNumber' created
Date: 04-11-2016
Signum Id:EAASTBA
*/

/**
 * @author ESIASAN
 *
 */
public class AddFnFNumberForMagicNumberRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = 1L;
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		/*if(!isNewSubscriber(fdpRequest)){
			final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
			 String notificationText = configurationMap.get(ConfigurationKey.INVALID_SUBSCRIBER_FOR_MAGIC_NUMBER.getAttributeName());
			 if (notificationText == null || notificationText.isEmpty()) {
				 notificationText = "Your number is not valid for this offer";
			 }
			return new FDPResponseImpl(Status.FAILURE, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_TERMINATE));}
		else */return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
		
	}
	
	private boolean isNewSubscriber(FDPRequest fdpRequest) throws ExecutionFailedException{
		String subscriberMsisdn= updateFaFMsisdn(fdpRequest,fdpRequest.getSubscriberNumber()+"");
		FDPCircle benMsisdnCircle = CircleCodeFinder.getFDPCircleByMsisdn(subscriberMsisdn, ApplicationConfigUtil.getApplicationConfigCache());
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, subscriberMsisdn);
		ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, true, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
		CommandUtil.executeCommand(fdpRequest, Command.GETACCOUNTDETAILS, true);
		FDPCommand fdpCommandGBAD = fdpRequest.getExecutedCommand(Command.GETACCOUNTDETAILS.getCommandDisplayName());
		Map<String, CommandParam> outputParamMap = fdpCommandGBAD.getOutputParams(); 
		Date activationDate = outputParamMap.get(FDPConstant.ACTIVATION_DATE).getValue()==null?null:
			((GregorianCalendar)outputParamMap.get(FDPConstant.ACTIVATION_DATE).getValue()).getTime();
		Calendar cal = Calendar.getInstance();
	    cal.setTime(new Date());
	    cal.add(Calendar.MONTH, -6);
	    Date date=cal.getTime();
	    return date.compareTo(activationDate)>=0;
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {
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
		
		if(!isBeneficiaryFaFValid(fdpRequest, input.toString())){
			return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, "Request is invalid for beneficiary number", null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Request is invalid for beneficiary number",
							TLVOptions.SESSION_TERMINATE));
		}
		String benMsisdn = ((FDPSMPPRequestImpl)fdpRequest).getAuxiliaryRequestParameter(AuxRequestParam.MAGIC_NUMBER).toString();
		if(benMsisdn.equals(incomingSubscriber.toString()))
			return response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, requestString, null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger),
							TLVOptions.SESSION_TERMINATE));		
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT, input.toString());
		} catch (NotificationFailedException e) {
			e.printStackTrace();
		}
			
		return response;
	}

	private boolean isBeneficiaryFaFValid(FDPRequest fdpRequest,String beneficiaryMsisdn) throws ExecutionFailedException{
		Boolean isBeneficiaryMsisdnValid = false;
		FDPCircle benMsisdnCircle = CircleCodeFinder.getFDPCircleByMsisdn(beneficiaryMsisdn, ApplicationConfigUtil.getApplicationConfigCache());
		if(null != benMsisdnCircle && benMsisdnCircle.getCircleName().contentEquals(fdpRequest.getCircle().getCircleName())){
			beneficiaryMsisdn = updateFaFMsisdn(fdpRequest,beneficiaryMsisdn);
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, beneficiaryMsisdn);	
			ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, true, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
			CommandUtil.executeCommand(fdpRequest, Command.GET_BALANCE_AND_DATE, true);
			ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, false, LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, null);
			FDPCommand fdpCommand = fdpRequest.getExecutedCommand(Command.GET_BALANCE_AND_DATE.getCommandDisplayName());
			if(!fdpCommand.getResponseError().getResponseCode().contentEquals("102")){
				isBeneficiaryMsisdnValid = true;
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.MAGIC_NUMBER, beneficiaryMsisdn);
			}
		}else{
			FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(), "isBeneficiaryMsisdnValid", 
					"Request is invalid for beneficiary msisdn : " + beneficiaryMsisdn);
		}
		return isBeneficiaryMsisdnValid;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	public String getNotificationText(FDPRequest fdpRequest){
		 final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		 String notificationText = configurationMap.get(ConfigurationKey.ADD_FAF_FOR_MAGIC_NUMBER.getAttributeName());
		 if (notificationText == null || notificationText.isEmpty()) {
			 notificationText = "Please enter FaF mobile number";
		 }
		 return notificationText;
	}
	
	public String updateFaFMsisdn(FDPRequest fdpRequest, String msisdn) throws ExecutionFailedException{
		final Map<String, String> configurationsMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		Integer allowedLength = Integer.parseInt(configurationsMap
				.get(ConfigurationKey.MSISDN_NUMBER_LENGTH
						.getAttributeName()));
		if (msisdn.length() <= allowedLength) {
			final FDPAppBag bag = new FDPAppBag();
			bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
			bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
			final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil
					.getApplicationConfigCache();
			final String countryCode = (String) fdpCache.getValue(bag);
			msisdn = countryCode + msisdn;
		}
		return msisdn;
		
	}
}