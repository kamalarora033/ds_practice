package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.math.NumberUtils;

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
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;


/*
Feature Name: User can purchase bundle for self and others
Changes: New policy 'BuyForOtherMsisdnPolicyRuleImpl' created
Date: 28-10-2015
Singnum Id:ESIASAN
*/

/**
 * @author ESIASAN
 *
 */
public class BuyForOtherMsisdnPolicyRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = 1L;
	
	
	
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		Object isBuyForOtherObject = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IS_BUY_FOR_OTHER);
		if(isBuyForOtherObject != null && isBuyForOtherObject instanceof Boolean && (Boolean) isBuyForOtherObject)			
				return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
		return null;
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) 
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);		
		else if (PolicyRuleValidateImpl.isNullorEmpty(input)) {
					String benmsisdn=updateBeneficiaryMsisdn(fdpRequest, input.toString());					
			if (benmsisdn == null) 					
				response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE,getInvalidMsisdnText(fdpRequest), null, true,
						ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), getInvalidMsisdnText(fdpRequest),
								TLVOptions.SESSION_TERMINATE));
			else 
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN, benmsisdn);					
		}
		else{
			response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),  PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
							TLVOptions.SESSION_TERMINATE));
		}
		
		return response;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	
	/**
	 * 
	 */
	@Override
	public String getNotificationText(FDPRequest fdpRequest){
		 final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		 String notificationText = configurationMap.get(ConfigurationKey.BUY_OTHER_MSISDN_TEXT.getAttributeName());
		 if (notificationText == null || notificationText.isEmpty()) {
			 notificationText = "Please enter beneficiary mobile number";
		 }
		 return notificationText;
	}
	
	/**
	 * This method will append the country code in the provided msisdn
	 * @param fdpRequest
	 * @param msisdn
	 * @return the valid msisdn
	 * @throws ExecutionFailedException
	 */
	public String updateBeneficiaryMsisdn(FDPRequest fdpRequest, String msisdn) throws ExecutionFailedException{
		String msisdnWithCountryCode = null;
		if (!isNumber(msisdn))
			return msisdnWithCountryCode;
		final Map<String, String> configurationsMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		Integer allowedLength = Integer.parseInt(configurationsMap.get(ConfigurationKey.MSISDN_NUMBER_LENGTH.getAttributeName()));
		final FDPAppBag bag = new FDPAppBag();
		bag.setKey(ConfigurationKey.COUNTRY_CODE.getAttributeName());
		bag.setSubStore(AppCacheSubStore.CONFIGURATION_MAP);
		final FDPCache<FDPAppBag, Object> fdpCache = ApplicationConfigUtil.getApplicationConfigCache();
		final String countryCode = (String) fdpCache.getValue(bag);		
		if (msisdn.length() == allowedLength || (msisdn.startsWith(FDPConstant.MSISDN_WITH_PREFIX_ZERO) && msisdn.length() == (allowedLength+1))) {			
			msisdnWithCountryCode = countryCode + msisdn;
			if(msisdn.startsWith(FDPConstant.MSISDN_WITH_PREFIX_ZERO))
				msisdnWithCountryCode =  countryCode + msisdn.substring(1);
							
		} 
		else if(msisdn.length() == allowedLength + countryCode.length() && msisdn.startsWith(countryCode)) 
			msisdnWithCountryCode =  msisdn;
		
		return msisdnWithCountryCode;
	}
	
	/**
	 * This method would return the invalid msisdn notification text 
	 * @param fdpRequest
	 * @return the Invalid Notification
	 */
	private String getInvalidMsisdnText(FDPRequest fdpRequest){			
		 final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		 String notificationText = configurationMap.get(ConfigurationKey.BUY_OTHER_INVALID_MSISDN_TEXT.getAttributeName());
		 if(notificationText!=null)
			 return notificationText;		 
		return PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, "Beneficiary mobile number is invalid");
	}
	
	/**
	 * This method will match the provided MSISDN with digit [0-9]. if the MSISDN contain only digit
	 * between [0-9] then it will return true else false
	 * @param msisdn
	 * @return
	 */
	private boolean isNumber(String msisdn) {
		Pattern pattern = Pattern.compile(".*[^0-9].*");
		return !pattern.matcher(msisdn).matches();
	    
	}
}