package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.ProvisioningTypeEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.product.Product;
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
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * @author EAMASAC
 *
 */
public class OptInBuyProductPolicyRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = -8636438791614204837L;
	
	/* (non-Javadoc)
	 * @see com.ericsson.fdp.business.policy.policyrule.impl.OptInBuyProductPolicyRuleImpl#displayRule(com.ericsson.fdp.core.request.FDPRequest)
	 */
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		if(evaluateAdditionalPolicyRule(fdpRequest, ProductAdditionalInfoEnum.RS_OPT_IN_OPT_OUT)) {
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
		}
		return fdpResponse;
	
	}

	/* (non-Javadoc)
	 * @see com.ericsson.fdp.business.policy.policyrule.impl.OptInBuyProductPolicyRuleImpl#validatePolicyRule(java.lang.Object, com.ericsson.fdp.core.request.FDPRequest, java.lang.Object[])
	 */
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		}else{
			try {
				Integer type = FDPConstant.NORMAL_PRODUCT_TYPE;
				String validConfirmationInputs = getConfigurationMapValue(fdpRequest, ConfigurationKey.BUY_OPTIN_OPTOUT_PRODUCT_VALID_RESPONSE);
				validConfirmationInputs = (null != validConfirmationInputs && !validConfirmationInputs.isEmpty()) ? validConfirmationInputs : "1";
				String validInputs[] = validConfirmationInputs.split(","); 
				for(int i = 0; i < validInputs.length; i++){
					if(validInputs[i].trim().contentEquals(input.toString())){
						type = FDPConstant.OPTIN_OPTOUT_PRODUCT_TYPE;
						break;
					}
				}
				if(FDPConstant.NORMAL_PRODUCT_TYPE==type) {
					updateParams(fdpRequest, input.toString());
				}
				((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TYPE, type);
			} catch (final Exception e) {
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	

	/**
	 * This method will return the value of input configuration key as defined in fdpCircle
	 * @return
	 */
	private String getConfigurationMapValue(FDPRequest fdpRequest, ConfigurationKey key){
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationMap.get(key.getAttributeName());		
	}
	
	public String getNotificationText(FDPRequest fdpRequest) {
		String notificationText = getConfigurationMapValue(fdpRequest, ConfigurationKey.CONFIRM_PRODUCT_AUTO_RENEWAL);
	    if (notificationText == null || notificationText.isEmpty()) {
		   notificationText = "Press 1 to confirm auto renewal 2 to cancel";
	    }
	    return notificationText;
	}
	
	private void updateParams(final FDPRequest fdpRequest, final String input) {
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.provisioningType, ProvisioningTypeEnum.ADHOC.getType());
		((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_ACTION_TYPE, FDPConstant.OPT_IN);
	}
}

