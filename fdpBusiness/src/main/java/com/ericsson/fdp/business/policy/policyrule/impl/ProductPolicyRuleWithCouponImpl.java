package com.ericsson.fdp.business.policy.policyrule.impl;


import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ChargingTypeNotifications;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.enums.mcoupon.CouponPolicyIndex;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.policy.policyrule.PolicyRule;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

public class ProductPolicyRuleWithCouponImpl extends AbstractPolicyRule implements PolicyRule {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 423421234457886561L;

	protected static final String ZERO_CHARGING_AMOUNT = "0";

	/**
	 * The list of valid responses.
	 */
	// private static final List<String> validResponses = Arrays.asList("Yes",
	// "YES", "0");

	@Override
	public FDPResponse displayRule(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		String notificationText = getNotificationText(fdpRequest);
		if (notificationText != null) {
			final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
			notificationText = notificationText + FDPConstant.NEWLINE + configurationMap.get(ConfigurationKey.PRODUCT_BUY_APPLY_COUPON_CHOICE_TEXT.getAttributeName());
			fdpResponse = new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), notificationText, TLVOptions.SESSION_CONTINUE));
			// LoggerUtil.generateProductBehaviourLogsForUserBehaviour(fdpRequest,
			// ProductBehaviourStatus.REQUESTED);
		}
		return fdpResponse;
	}

	/**
	 * This method is used to get notification text from request.
	 * 
	 * @param fdpRequest
	 *            the request from which notification text is to be created.
	 * @return the notification text to be used.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private String getNotificationText(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Product product = RequestUtil.getMetaValueFromRequest(fdpRequest, RequestMetaValuesKey.PRODUCT,
				Product.class);
		final ChargingValue chargingAmount = RequestUtil.getMetaValueFromRequest(fdpRequest,
				RequestMetaValuesKey.CHARGING_STEP, ChargingValue.class);
		return getNotificationText(product, chargingAmount, fdpRequest);
	}

	/**
	 * This method is used to get the notification text.
	 * 
	 * @param product
	 *            the product.
	 * @param chargingAmount
	 *            the charging amount.
	 * @param fdpRequest
	 *            the request object to be used.
	 * @return the response.
	 * @throws ExecutionFailedException
	 */
	protected String getNotificationText(final Product product, final ChargingValue chargingAmount,
			final FDPRequest fdpRequest) throws ExecutionFailedException {
		final ChargingTypeNotifications chargingTypeNotifications = (ZERO_CHARGING_AMOUNT.equals(chargingAmount
				.getChargingValue().toString())) ? ChargingTypeNotifications.ZERO_CHARGING_BUY_PRODUCT
				: ChargingTypeNotifications.POSITIVE_CHARGING_BUY_PRODUCT;
		return getNotificationTextForChargingType(product, fdpRequest, chargingTypeNotifications);
	}

	/**
	 * Gets the notification text for charging type.
	 * 
	 * @param product
	 *            the product
	 * @param fdpRequest
	 *            the fdp request
	 * @param chargingTypeNotifications
	 *            the charging type notifications
	 * @return the notification text for charging type
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected String getNotificationTextForChargingType(final Product product, final FDPRequest fdpRequest,
			final ChargingTypeNotifications chargingTypeNotifications) throws ExecutionFailedException {
		String notificationText = null;
		final Long fdpNotificationId = product.getNotificationIdForChannel(fdpRequest.getChannel(),
				chargingTypeNotifications);
		final Logger logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(logger, getClass(), "getNotificatioNText", "creating notification text for product buy");
		if (fdpNotificationId != null) {
			try {
				notificationText = NotificationUtil.createNotificationText(fdpRequest, fdpNotificationId, logger);
			} catch (final NotificationFailedException e) {
				FDPLogger.error(logger, getClass(), "getNotificatioNText", "Could not create notification text", e);
				throw new ExecutionFailedException("Could not create notification text", e);
			}
		}
		return notificationText;
	}

	@Override
	public AuxRequestParam getAuxiliaryParam() {
		return null;
	}

	@Override
	public FDPPolicyResponse validatePolicyRule(final Object input, final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response = null;
		LoggerUtil.generatePolicyBehaviourLogsForUserBehaviour(fdpRequest, AuxRequestParam.CONFIRMATION.getName());
		if ((input == null && getNotificationText(fdpRequest) != null)) {
			throw new ExecutionFailedException("Cannot validate policy value");
		} else if (input != null) {
			final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
			final String validResponsesForProductBuy = configurationMap
					.get(ConfigurationKey.PRODUCT_BUY_VALID_RESPONSES.getAttributeName());
			boolean isValid = false;
			if (validResponsesForProductBuy != null && !validResponsesForProductBuy.isEmpty()) {
				final String[] validResponses = validResponsesForProductBuy.split(FDPConstant.COMMA);
				for (final String validResponse : validResponses) {
					if (validResponse.equalsIgnoreCase(input.toString())) {
						isValid = true;
						response = new FDPPolicyResponseImpl(PolicyStatus.GOTO_RULE, null, null, true, null);
						response.setNextRuleIndex(CouponPolicyIndex.LOAN_PROCESS_POLICY_RULE.getIndex());
						break;
					}
				}

			}
			
			final String validResponsesForProductBuyWithCoupon = configurationMap
					.get(ConfigurationKey.PRODUCT_BUY_APPLY_COUPON_CHOICE.getAttributeName());
			if (validResponsesForProductBuyWithCoupon != null && !validResponsesForProductBuyWithCoupon.isEmpty()) {
				final String[] validResponses = validResponsesForProductBuyWithCoupon.split(FDPConstant.COMMA);
				for (final String validResponse : validResponses) {
					if (validResponse.equalsIgnoreCase(input.toString())) {
						isValid = true;
						response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
						//response.setNextRuleIndex(CouponPolicyIndex.PRODUCT_BUY_APPLY_COUPON_RULE.getIndex());
						break;
					}
				}

			}
			
			if (!isValid) {
				response = getResponseForInvalidInput(input, fdpRequest, configurationMap);
			}
		}
		return response;
	}

	/**
	 * This method is used to get response for invalid input.
	 * 
	 * @param input
	 *            the input.
	 * @param fdpRequest
	 *            the request.
	 * @param configurationMap
	 *            the configuration map.
	 * @return the response.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPPolicyResponse getResponseForInvalidInput(final Object input, final FDPRequest fdpRequest,
			final Map<String, String> configurationMap) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		String responseString = configurationMap.get(ConfigurationKey.INVALID_INPUT_STRING.getAttributeName());
		if (responseString == null || responseString.isEmpty()) {
			responseString = "Input invalid";
		}
		final String invalidResponsesForProductBuy = configurationMap
				.get(ConfigurationKey.PRODUCT_BUY_INVALID_RESPONSES.getAttributeName());
		boolean isInValid = false;
		if (invalidResponsesForProductBuy != null && !invalidResponsesForProductBuy.isEmpty()) {
			final String[] invalidResponses = invalidResponsesForProductBuy.split(FDPConstant.COMMA);
			for (final String invalidResponse : invalidResponses) {
				if (invalidResponse.equalsIgnoreCase(input.toString())) {
					isInValid = true;
					final String rejectedString = configurationMap.get(ConfigurationKey.REJECTED_INPUT_STRING
							.getAttributeName());
					final String resultString = (rejectedString == null) ? responseString : rejectedString;
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, resultString, null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), resultString,
									TLVOptions.SESSION_TERMINATE));
					break;
				}
			}
		}
		if (!isInValid) {
			response = getResponse(responseString, fdpRequest, ClassUtil.getLongValue(configurationMap
					.get(ConfigurationKey.PRODUCT_RETRY_NUMBER.getAttributeName())));
		}
		return response;
	}

	/**
	 * This method is used to get response.
	 * 
	 * @param responseString
	 *            the response string.
	 * @param retryNumber
	 *            the retry number.
	 * @return the response formed.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private FDPPolicyResponse getResponse(final String responseString, final FDPRequest fdpRequest,
			final Long retryNumber) throws ExecutionFailedException {
		FDPPolicyResponse fdpPolicyResponse = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, responseString, null,
				true, ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), responseString,
						TLVOptions.SESSION_TERMINATE));
		if (retryNumber != null && retryNumber > 0) {
			final Long currentRetry = (Long) fdpRequest
					.getAuxiliaryRequestParameter(AuxRequestParam.POLICY_RETRY_VALUE);
			if (currentRetry == null || currentRetry + 1 <= retryNumber) {
				final String notText = getNotificationText(fdpRequest);
				fdpPolicyResponse = new FDPPolicyResponseImpl(PolicyStatus.RETRY_POLICY, notText,
						currentRetry == null ? 1 : currentRetry + 1, false, ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(), notText, TLVOptions.SESSION_CONTINUE));
			}
		}
		return fdpPolicyResponse;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
}
