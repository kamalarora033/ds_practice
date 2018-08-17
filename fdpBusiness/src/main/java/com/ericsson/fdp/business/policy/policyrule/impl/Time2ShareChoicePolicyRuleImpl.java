package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;


/**
 * This policy will show list of accounts from which the subscriber can share amount to other subscriber.
 * @author ESIASAN
 *
 */
public class Time2ShareChoicePolicyRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = 3067109362915972915L;

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false,
				ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
	}

	/**
	 * This method will validate the user choice of time2share account and store his preference in AUX_REQUEST_PARAM
	 */
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		boolean isValid = input!=null ?StringUtil.isStringIntegerType(input.toString()):false;
		try {
			if (isValid) {
				isValid = false;
				Integer inputValue = Integer.parseInt(input.toString());
				LinkedHashMap<String, String> time2shareAccountsMap = getTime2ShareAccountsMap(fdpRequest);
				int i = 1;
				for (Map.Entry<String, String> account : time2shareAccountsMap.entrySet()) {
					if (i == inputValue) {
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
								AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_FROM, account.getValue());
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
								AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_TO, account.getValue());
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.IS_TIME2SHARE, true);
						isValid = true;
						break;
					}
					i++;
				}
				if (!isValid) {

					final String inValidInputText = invalidInputText(fdpRequest);
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, inValidInputText, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), inValidInputText,
									TLVOptions.SESSION_TERMINATE));
				}
			}

		} catch (Exception e) {
			throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
		}
		
		return response;
	}
	
	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	/**
	 * This method returns the transfer menu options for time2share
	 */
	public String getNotificationText(FDPRequest fdpRequest) {
		StringBuilder notificationText = new StringBuilder("Please choose the account:\n");
		LinkedHashMap<String, String> time2shareAccountsMap = getTime2ShareAccountsMap(fdpRequest);
		int i = 1;
		for(Map.Entry<String, String> account : time2shareAccountsMap.entrySet()){
			notificationText.append(i + ". " + account.getKey() + "\n");
			i++;
		}
		return notificationText.toString();
	}
	
	/**
	 * This method returns the map of all the accounts from which source number can perform time2share
	 * @return
	 */
	private LinkedHashMap<String, String> getTime2ShareAccountsMap(FDPRequest fdpRequest){
		LinkedHashMap<String, String> time2shareAccountsMap = new LinkedHashMap<String, String>();
		String time2shareAccountsStr = getConfigurationMapValue(fdpRequest, ConfigurationKey.TIME2SHARE_ENABLED_ACCOUNTS);
		time2shareAccountsStr = (null == time2shareAccountsStr) ? "Main Account:0" : time2shareAccountsStr;
		String time2shareAccounts[] = time2shareAccountsStr.split(","); 
		for(int i = 0; i < time2shareAccounts.length; i++){
			String account[] = time2shareAccounts[i].split(":");
			time2shareAccountsMap.put(account[0], account[1]);
		}
		return time2shareAccountsMap;
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