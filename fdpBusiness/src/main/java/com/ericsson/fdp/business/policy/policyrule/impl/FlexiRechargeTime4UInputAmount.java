package com.ericsson.fdp.business.policy.policyrule.impl;

import ch.qos.logback.classic.Logger;
import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.ChargingTypeNotifications;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.business.util.TariffEnquiryNotificationUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * @author EAMASAC
 *
 */
public class FlexiRechargeTime4UInputAmount extends ProductPolicyRuleImpl{


	private static final long serialVersionUID = -8636438791614204867L;
	
	
	
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		if(evaluateAdditionalPolicyRule(fdpRequest, ProductAdditionalInfoEnum.TIME4U_INPUT_AMOUNT)) {
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
		}
		return fdpResponse;
	}
	
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		}else{
			try {
				
				if(!PolicyRuleValidateImpl.isNullorEmpty(input) || !isValidAmount(fdpRequest, input.toString())){
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, "Entered Invalid amount in the request", null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Entered Invalid amount in the request",
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final Exception e) {
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}

	/**
	 * validate user input.
	 * 
	 * @param fdpRequest
	 * @param string
	 * @return
	 */
	private boolean isValidAmount(FDPRequest fdpRequest, String string) {
		Boolean isValidAmount = false;
		try {
			if(null != string){
				FulfillmentUtil.updateParameterForTime4U(fdpRequest, Integer.parseInt(string));
				isValidAmount = true;
			}else{
				FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(), "isValidAmount", 
						"Invalid amount in the request: " + string);
			}
		} catch (final Exception e) {
			e.printStackTrace();
			isValidAmount = false;
		}
		return isValidAmount;
	
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	protected String getNotificationText(FDPRequest fdpRequest){
		FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		String notificationText = null;
		if (fdpCacheable instanceof Product) {
			Product product = (Product) fdpCacheable;
			final Long notificationId = product.getNotificationIdForChannel(fdpRequest.getChannel(),
					ChargingTypeNotifications.TIME4U_INPUT_AMOUNT);
			final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
			try {
				notificationText = TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationId,
						circleLogger);
			} catch (NotificationFailedException e) {
				e.printStackTrace();
				notificationText = "Please enter the amount:";
			}
		}
		return notificationText;
	}

}