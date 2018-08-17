package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

/**
 * This class is used to take subscriber confirmation for to transfer data to other subscriber
 * @author GUR21122
 * @version 1.0
 * @since 2017-10-16
 *
 */

public class Data2ShareConformPolicyRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = 1L;
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		return new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response=new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		}else{
			try {
				
				boolean isValid =PolicyRuleValidateImpl.isNullorEmpty(input);
				if (isValid) {
					isValid = false;
					String validConfirmationInputs = getConfigurationMapValue(fdpRequest,
							ConfigurationKey.DATA2SHARE_CONFIRMATION_VALID_INPUT);
					validConfirmationInputs = (null != validConfirmationInputs && !validConfirmationInputs.isEmpty()) ? validConfirmationInputs
							: "1";
					if (validConfirmationInputs.contentEquals(input.toString())) {
						isValid = true;
						setDAIdInRequest(fdpRequest);
					}
					
				}else{
					isValid = false;
				}
				
				
				String validCancellationInputs = getConfigurationMapValue(fdpRequest,
						ConfigurationKey.DATA2SHARE_CANCELLATION_VALID_INPUT);
				validCancellationInputs = (null != validCancellationInputs && !validCancellationInputs.isEmpty()) ? validCancellationInputs
						: "9";
				if (input != null && validCancellationInputs.contentEquals(input.toString())) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, getCancelNotification(fdpRequest),
							null, true,
							ResponseUtil.createResponseMessageInList(
									fdpRequest.getChannel(), getCancelNotification(fdpRequest),
									TLVOptions.SESSION_TERMINATE));
					return response;
				}
				
				if (!isValid) {
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE,
							PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(),
									PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
									TLVOptions.SESSION_TERMINATE));
				}
				
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
	
	@Override
	public String getNotificationText(FDPRequest fdpRequest) throws ExecutionFailedException{
		String staticMsg="Please confirm purchase of GHC ${price} Data Bundle for ${benMsisdn} valid for ${validity} ${validityUnit}, 1. Confirm, 9. Cancel";
		String notificationText =   RequestUtil.getNotificationText(fdpRequest, ConfigurationKey.DATA2SHARE_CONFIRMATION_NOTIFICATION, staticMsg);
		
		/*final Product product = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.NODE,
				Product.class);*/
		
		
		  final ProductNode productNode = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.NODE,
					ProductNode.class);
		  if(productNode==null)
			  return "Please confirm purchase of data to share bundle, 1. confirm, 9. Cancel";
		  String productId = productNode.getEntityIdForCache(RequestMetaValuesKey.PRODUCT);
		  final Product product = RequestUtil.getProductById(fdpRequest, productId);
		  final FDPSMPPRequestImpl fdpussdsmscRequestImpl = (FDPSMPPRequestImpl) fdpRequest;
		  fdpussdsmscRequestImpl.addMetaValue(RequestMetaValuesKey.PRODUCT, product);
		
		if (notificationText.contains("${price}")) {
			Long productPrice = ChargingUtil.getApplicableProductChargingForProvisioning(fdpRequest);
			String csConversionFactor = getConfigurationMapValue(fdpRequest, ConfigurationKey.CS_CONVERSION_FACTOR);
			double costWithDecimalproductPricealue = productPrice.doubleValue();
			costWithDecimalproductPricealue = costWithDecimalproductPricealue/Integer.parseInt(csConversionFactor);
			notificationText = notificationText.replace("${price}", Double.toString(costWithDecimalproductPricealue));
		}
		
		if (notificationText.contains("${validity}"))
			notificationText = notificationText.replace("${validity}", product.getRecurringOrValidityValue());
		
		if (notificationText.contains("${validityUnit}"))
			notificationText = notificationText.replace("${validityUnit}", product.getRecurringOrValidityUnit());
		
		if (notificationText.contains("${benMsisdn}"))
			notificationText = notificationText.replace("${benMsisdn}", 
					fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT).toString());
		
		//Check for skip charging enable for Data2Share product or not. If skip charging is enable then charging shall be done 
		// By explicitly configuring UBAD in SP
		String skipCharging = RequestUtil.getNotificationText(fdpRequest, ConfigurationKey.DATA2SHARE_SKIP_CHARGING, "true");
		if (skipCharging.equalsIgnoreCase("true"))
			((FDPSMPPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, true);
		
		return notificationText;
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
	 * Set the DA Ids for debit and credit
	 * @param fdpRequest
	 * @throws ExecutionFailedException
	 */
	private void setDAIdInRequest(final FDPRequest fdpRequest) throws ExecutionFailedException{
		final Long fromDA = Long
				.parseLong(getConfigurationMapValue(fdpRequest, ConfigurationKey.DATA2SHARE_DA_ACCOUNT_FROM));
		final Long toDA = Long
				.parseLong(getConfigurationMapValue(fdpRequest, ConfigurationKey.DATA2SHARE_DA_ACCOUNT_TO));

		if (null != fromDA && null != toDA) {
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.DATA2SHARE_SUBSCRIBER_DAID,
					fromDA);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.DATA2SHARE_BENEFICIARY_DAID,
					toDA);
		}else{
			throw new ExecutionFailedException("DA id should not empty");
		}
	}

	/**
	 * This method will return error notification text
	 * @param fdpRequest
	 * @return
	 */
	private String getCancelNotification(FDPRequest fdpRequest) {
		String notificationText = getConfigurationMapValue(fdpRequest,
				ConfigurationKey.DATA2SHARE_CANCEL_TEXT);
		if (notificationText == null || notificationText.isEmpty()) {
			notificationText = "Purchase cancelled. Please Dial *138# for all your Bundles on MTN";
		}
		return notificationText;
	}

}
