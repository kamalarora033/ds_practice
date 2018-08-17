package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.node.FDPServiceProvisioningNode;
import com.ericsson.fdp.business.node.impl.ProductNode;

import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;


/*
Feature Name: User can purchase bundle for self and others
Changes: New policy 'BuyForOtherChoicePolicyRuleImpl' created
Date: 28-10-2015
Singnum Id:ESIASAN
*/

/**
 * @author ESIASAN
 *
 */
public class BuyForOtherChoicePolicyRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = 1L;
	
	
	
	
	/*
	 * If product is of type <buy-other>, then show the appropriate
	 * option to buy the product for self or for other
	 */
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		final FDPCacheable fdpCacheable = fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		boolean isBuyForOtherEnabled = false;
		if (fdpCacheable instanceof Product) {
			final Product product = (Product) fdpCacheable;
			isBuyForOtherEnabled = (null != product
					.getAdditionalInfo(ProductAdditionalInfoEnum.PRODUCT_FOR_OTHER)) ? Boolean
					.parseBoolean(product
							.getAdditionalInfo(ProductAdditionalInfoEnum.PRODUCT_FOR_OTHER))
					: false;
			if (isBuyForOtherEnabled && isProductSpTypeValid(fdpRequest)) {
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, false,
						ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(),
								getNotificationText(fdpRequest),
								TLVOptions.SESSION_CONTINUE));
			}
		}
		return fdpResponse;
	}

	/**
	 * This method check if requested node is of product buy type only.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private boolean isProductSpTypeValid(FDPRequest fdpRequest) {
		boolean isValid = false;
		try {
			if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof FDPNode) {
				final FDPNode fdpNode = (FDPNode) fdpRequest
						.getValueFromRequest(RequestMetaValuesKey.NODE);
				if (fdpNode instanceof ProductNode) {
					final ProductNode productNode = (ProductNode) fdpNode;
					if (productNode.getServiceProvSubType() != null) {
						isValid = FDPServiceProvSubType.PRODUCT_BUY.equals(productNode.getServiceProvSubType())
								|| FDPServiceProvSubType.PRODUCT_BUY_RECURRING.equals(productNode.getServiceProvSubType())
								|| FDPServiceProvSubType.BALANCE_ENQUIRY.equals(productNode.getServiceProvSubType())
								|| FDPServiceProvSubType.PRODUCT_BUY_SPLIT.equals(productNode.getServiceProvSubType());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return isValid;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(
				PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(
					PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false,
					null);
		} else  {
			try {
				
				Boolean isValid = PolicyRuleValidateImpl.isNullorEmpty(input);
				if (isValid) {
					isValid = false;
					final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
					final String validResponsesForBuyOtherChoice = configurationMap
							.get(ConfigurationKey.BUY_OTHER_CHOICE_VALID_RESPONSES.getAttributeName());
					final String validResponsesForBuySelfChoice = configurationMap
							.get(ConfigurationKey.BUY_SELF_CHOICE_VALID_RESPONSES.getAttributeName());
					if (validResponsesForBuyOtherChoice != null && !validResponsesForBuyOtherChoice.isEmpty()) {
						final String[] validResponses = validResponsesForBuyOtherChoice.split(FDPConstant.COMMA);
						for (final String validResponse : validResponses) {
							if (validResponse.equalsIgnoreCase(input.toString())) {
								isValid = true;
								((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
										AuxRequestParam.IS_BUY_FOR_OTHER, Boolean.TRUE);
								break;
							}
						}
					}
					if (validResponsesForBuySelfChoice != null && !validResponsesForBuySelfChoice.isEmpty()) {
						final String[] validResponses = validResponsesForBuySelfChoice.split(FDPConstant.COMMA);
						for (final String validResponse : validResponses) {
							if (validResponse.equalsIgnoreCase(input.toString())) {
								isValid = true;
								break;
							}
						}
					}
				}
				if (!isValid) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "The input is incorrect",
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final Exception e) {
				throw new ExecutionFailedException(
						"The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}
	
	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	/*
	 * Gets the policy text to show from configuration key BUY_OTHER_CHOICE_TEXT
	 * 
	 * @param fdpRequest
	 */
	public String getNotificationText(FDPRequest fdpRequest) {
		final Map<String, String> configurationMap = fdpRequest.getCircle()
				.getConfigurationKeyValueMap();
		String notificationText = configurationMap
				.get(ConfigurationKey.BUY_OTHER_CHOICE_TEXT.getAttributeName());
		if (notificationText == null || notificationText.isEmpty()) {
			notificationText = "Press 1 for buy for self \n 2 for buy for other";
			return notificationText;
		}else{
			String[] arr = notificationText.split(",");
			StringBuilder notText = new StringBuilder(FDPConstant.EMPTY_STRING);
			for(int index =0;index < (arr.length-1);index++)
				notText.append(arr[index]+"\n");
			notText.append(arr[arr.length-1]);
			return notText.toString();
		}
		
	}

}