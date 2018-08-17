package com.ericsson.fdp.business.policy.policyrule.impl;

import ch.qos.logback.classic.Logger;
import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ChargingTypeNotifications;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.TariffEnquiryNotificationUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * Genric User Policy for UserInput.
 * 
 * @author eashtod
 *
 */
public class UserInputThresholdPolicyImpl extends ProductPolicyRuleImpl{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8636438791614204867L;
	
	
	
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		 FDPResponse fdpResponse = null;
		 if (evaluateAdditionalPolicyRule(fdpRequest, ProductAdditionalInfoEnum.USER_THRESHOLD_POLICY)) {
			  fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
				fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
		 }
		return fdpResponse;
	}
	
	/**
	 * This method checks user input.
	 */
	protected String getNotificationText(FDPRequest fdpRequest){
		FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		String notificationText = null;
		if (fdpCacheable instanceof Product) {
			Product product = (Product) fdpCacheable;
			final Long notificationId = product.getNotificationIdForChannel(fdpRequest.getChannel(),
					ChargingTypeNotifications.USER_THRESHOLD_POLICY);
			final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
			try {
				notificationText = TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationId,
						circleLogger);
			} catch (NotificationFailedException e) {
				e.printStackTrace();
				notificationText = "Please enter theshold value:";
			}
		}
		return notificationText;
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		}else{
			try {
				
				if(!PolicyRuleValidateImpl.isNullorEmpty(input) || !validateUserInput(fdpRequest, input.toString())){
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final Exception e) {
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}

	/**
	 * This method checks is user input.
	 * 
	 * @param fdpRequest
	 * @param string
	 * @return
	 */
	private boolean validateUserInput(FDPRequest fdpRequest, String userInput) {
		boolean valid = false;
		if (!StringUtil.isNullOrEmpty(userInput) && userInput.length() < 5) {
			try {
				Integer input = Integer.parseInt(userInput);
				((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(
						AuxRequestParam.CONSUMER_LIMIT_UPGRADE_VALUE, input);
				valid = true;
			} catch (Exception e) {
				e.printStackTrace();
				valid = false;
			}
		}
		return valid;
	}
}
