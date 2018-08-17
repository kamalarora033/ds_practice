package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.FnfUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * Feature : Add Friends and Family Policy for Local/International number
 * choice.
 * 
 * @author evasaty
 * 
 */
public class AddFnfNumberChoicePolicyRuleImpl extends ProductPolicyRuleImpl {

	private static final long serialVersionUID = 1L;
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AddFnfNumberChoicePolicyRuleImpl.class);
	
	
	

	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse;
		final Product product = FnfUtil.getProduct(fdpRequest);
		if (product != null) {
			if (FnfUtil.isProductSpTypeValid(fdpRequest)) {
				//Check circle configuration of FAF and Magic Number changes done by eagarsh 
				final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
				final String validConfigurationForFafOrMagicNumber = configurationMap
						.get(ConfigurationKey.TYPE_FAF_OR_MAGIC_NUMBER.getAttributeName());
				if(null != validConfigurationForFafOrMagicNumber && !validConfigurationForFafOrMagicNumber.isEmpty() && validConfigurationForFafOrMagicNumber.equalsIgnoreCase(ProductType.FAF_NUMBER.getName())){
					
					
					// If user is not register for FAF. changes by eagarsh
					if(FDPConstant.FAF_MSISDN_ALREADY_REGISTER == FnfUtil.IsSubscriberAlreadyRegisterForFaf(fdpRequest)){
						fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
					}else{
						LOGGER.error("Product" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + product.getProductDescription()
								+ FDPConstant.LOGGER_METHOD_PARAMETER_SEPA + "User is not register for FAF.");
						fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(), "User is not registered for FAF.", TLVOptions.SESSION_TERMINATE));
					}
					
				}else if(null != validConfigurationForFafOrMagicNumber && !validConfigurationForFafOrMagicNumber.isEmpty() && validConfigurationForFafOrMagicNumber.equalsIgnoreCase(ProductType.MAGIC_NUMBER.getName())){
					//Request handling for Magic Number changes, if getFAF list is empty then call policy or terminate.
						Map<Integer, String> fafListResponseMap =  FnfUtil.getFriendsAndFamilyList(fdpRequest);
					if(fafListResponseMap.isEmpty()){
						// If fafList is empty, then cis will add magic no
						fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(), getNotificationTextForMagicNumber(fdpRequest), TLVOptions.SESSION_CONTINUE));
					}else{
						// If fafList is not empty, then cis will not any add magic no.
						LOGGER.info("Product" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + product.getProductDescription()
								+ FDPConstant.LOGGER_METHOD_PARAMETER_SEPA + "You have already added one magic. More than 1 no is not allowed.");
						fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(), "You have already added one magic. More than 1 no is not allowed. ", TLVOptions.SESSION_TERMINATE));
					}
					
					
				}else{
					// Invalid circle configuration for FAF and Magic Number for key TYPE_FAF_OR_MAGIC_NUMBER.
					LOGGER.error("Product" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + product.getProductDescription()
							+ FDPConstant.LOGGER_METHOD_PARAMETER_SEPA + "Invalid circle configuration for FAF and Magic Number for key TYPE_FAF_OR_MAGIC_NUMBER.");
					fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
							fdpRequest.getChannel(), "Invalid Product configuration", TLVOptions.SESSION_TERMINATE));
				}
				
				
				
			} else {
				LOGGER.error("Product" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + product.getProductDescription()
						+ FDPConstant.LOGGER_METHOD_PARAMETER_SEPA + "Product is not of FAF type.");
				fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
						fdpRequest.getChannel(), "Product is not of FAF type.", TLVOptions.SESSION_TERMINATE));
			}
		} else {
			LOGGER.error("Product not found in cache." + "RID" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
					+ fdpRequest.getRequestId());
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), "Some exception occured. Please try after some time.",
					TLVOptions.SESSION_TERMINATE));
		}
		return fdpResponse;
	}

	

	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null) 
			return new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		
		if(condition != null)
			return new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		
		if(input.toString().trim().isEmpty()){
			return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
					ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
							TLVOptions.SESSION_TERMINATE));
		}
			try {
					final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
					final String validResponsesAddLocalChoice = configurationMap
							.get(ConfigurationKey.ADD_FAF_LOCAL_VALID_RESPONSES.getAttributeName());
					final String validResponsesAddInternationalChoice = configurationMap
							.get(ConfigurationKey.ADD_FAF_INTERNATIONAL_VALID_RESPONSES.getAttributeName());
					
					//Check Is input for FAF or Magic Number
					final String validConfigurationForFafOrMagicNumber = configurationMap
							.get(ConfigurationKey.TYPE_FAF_OR_MAGIC_NUMBER.getAttributeName());
					if(!StringUtil.isNullOrEmpty(validConfigurationForFafOrMagicNumber)  && validConfigurationForFafOrMagicNumber.equalsIgnoreCase(ProductType.FAF_NUMBER.getName())){
						//For faf number Input validation
						if(!StringUtil.isNullOrEmpty(validResponsesAddLocalChoice) && validResponsesAddLocalChoice.equalsIgnoreCase(input.toString())) {
							((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.IS_LOCAL_ADD_FNF,
										Boolean.TRUE);
						}
						if(!StringUtil.isNullOrEmpty(validResponsesAddLocalChoice) && validResponsesAddInternationalChoice.equalsIgnoreCase(input.toString())) {
								((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.IS_LOCAL_ADD_FNF,
										Boolean.FALSE);
							
						}
					}else{
						//For Magic number Input validation for Same as subscriber Number.
						if(validateSameMsisdn(input.toString(), fdpRequest)){
							return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
									ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.MAGIC_CANNOT_SAME_AS_MSISDN, null),
											TLVOptions.SESSION_TERMINATE));
						}
					
						if(!FnfUtil.validateMagicMsisdn(input.toString(), fdpRequest)){
							return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
									ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.INVALID_MSISDN_FORMAT, null),
											TLVOptions.SESSION_TERMINATE));
						}
							
								if(FnfUtil.validationAndexecutionForMagicNumber(fdpRequest)){
									return new FDPPolicyResponseImpl(PolicyStatus.SKIP_EXECUTION, null, null, false,ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.MSISDN_IS_NOT_ONNET, null),
											TLVOptions.SESSION_CONTINUE));
								}else{
									return new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
											ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.MSISDN_IS_NOT_ONNET, null),
													TLVOptions.SESSION_TERMINATE));
								}
						
					}
				} catch (final Exception e) {
				LOGGER.error("The policy rule could not be evaluated." + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
						+ this.getClass());
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
	
		return response;
	}

	
	
	/**
	 * Gets the policy text to show from configuration key Friends and Family
	 * add menu.
	 * @param fdpRequest
	 * @return notificationText
	 */
	@Override
	public String getNotificationText(FDPRequest fdpRequest) {
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String notificationText;
		notificationText = configurationMap.get(ConfigurationKey.ADD_FNF_DISPLAY_TEXT.getAttributeName());
		if (notificationText == null || notificationText.isEmpty()) {
			notificationText = "Select option to add under Friends and Family 1.Local Number[Onnet/Offnet] 2.International Number ";
		}
		return notificationText;
	}
	
	/**
	 * Get the policy text to show Magic Number menu to add number.
	 * @param fdpRequest
	 * @return
	 */
	private String getNotificationTextForMagicNumber(FDPRequest fdpRequest) {
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String notificationTextForMagicNo;
		notificationTextForMagicNo = configurationMap.get(ConfigurationKey.ADD_MAGIC_NUMBER.getAttributeName());
		if(notificationTextForMagicNo == null || notificationTextForMagicNo.isEmpty()){
			notificationTextForMagicNo = "Thank you for choosing MTN Free Call Offer. Enjoy 6 Months of Free calls with your choosen number. Enter your Friends 10 digit number.";
		}
		return notificationTextForMagicNo;
	}
	
	/**
	 * This method is use to check enter Magic Msisdn is not same like Subscriber Msisdn
	 * @param strInput
	 * @param fdpRequest
	 * @return
	 */
	private boolean validateSameMsisdn(String strInput, FDPRequest fdpRequest) {
		if(strInput.startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)){
			return fdpRequest.getSubscriberNumber().toString().endsWith(strInput.substring(2));
		}else{
			return fdpRequest.getSubscriberNumber().toString().endsWith(strInput);
		}
		
	}
}
