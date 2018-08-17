package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.impl.VariableCharging;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.PolicyStatus;
import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.TLVOptions;

import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.FDPPolicyResponse;
import com.ericsson.fdp.business.request.impl.FDPPolicyResponseImpl;
import com.ericsson.fdp.business.request.impl.FDPResponseImpl;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;
import com.ericsson.fdp.dao.enums.ChargingType;

/**
 * This policy will present the subscriber with payment choice in case multiple
 * charging options are available for a specific product else it will simply
 * skip
 * 
 * @author ESIASAN
 * @date 21-12-2015
 */

public class PaymentChoicePolicyRuleImpl extends AbstractPolicyRule {

	private static final long serialVersionUID = 1L;

	
	
	
	/**
	 * if multiple payment options are available then show options to user for
	 * payment, else set default charging option and skip this policy
	 */
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest)
			throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		final FDPCacheable fdpCacheable = fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			final Product product = (Product) fdpCacheable;
			List<ExternalSystem> paymentOptions = getValidPaymentOptions(
					fdpRequest, product);
			if (paymentOptions.size() == 1) {
				((FDPRequestImpl) fdpRequest)
						.setExternalSystemToCharge(paymentOptions.get(0));
			} else {
				fdpResponse = new FDPResponseImpl(
						Status.SUCCESS,
						false,
						ResponseUtil.createResponseMessageInList(
								fdpRequest.getChannel(),
								getNotificationText(fdpRequest, paymentOptions),
								TLVOptions.SESSION_CONTINUE));
			}
		}
		return fdpResponse;
	}

	/**
	 * This method will validate the input for this policy
	 * 
	 */
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input,
			FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(
				PolicyStatus.SUCCESS, null, null, true, null);

		
		try {
			
			boolean isValid = PolicyRuleValidateImpl.isInteger(input);
			final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
			if (isValid && fdpCacheable instanceof Product) {
				isValid = false;
				final Product product = (Product) fdpCacheable;
				List<ExternalSystem> paymentOptions = getValidPaymentOptions(fdpRequest, product);
				Integer inputInt = Integer.parseInt(input.toString());
				if (inputInt > 0 && inputInt <= (paymentOptions.size())) {
					for (Integer counter = 1; counter <= paymentOptions.size(); counter++) {
						if (counter.equals(inputInt)) {
							((FDPRequestImpl) fdpRequest).setExternalSystemToCharge(paymentOptions.get(counter - 1));
							response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
							isValid = true;
							break;
						}
					}
				}
				if (!isValid) {
					//final String inValidInputText = invalidInputText(fdpRequest);
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null), null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), PolicyRuleValidateImpl.errorMsg(PolicyValidationMessageEnum.ERROR_MSG, null),
									TLVOptions.SESSION_TERMINATE));
				}
			}
		} catch (Exception e) {
			throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
		}

		return response;
	}

	/**
	 * This method will return payment option available in product definition
	 * and valid for subscriber
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private List<ExternalSystem> getValidPaymentOptions(
			final FDPRequest fdpRequest, final Product product)
			throws ExecutionFailedException {
		
		List<ExternalSystem> paymentOptions = new ArrayList<ExternalSystem>();
		
		Object obj = ApplicationCacheUtil.getValueFromApplicationCache(
				AppCacheSubStore.CONFIGURATION_MAP, "PAY_SRC_DISPLAY_ORDER");
		
	
		
		if (obj != null) {
			String paySrc = obj.toString();
			String[] paySrcArray = paySrc.split(",");
			List<ProductCharging> productChargings = getApplicableProductCharging(
					fdpRequest, product);
			for (int i = 0; i < paySrcArray.length; i++) {
				for (ProductCharging productCharging : productChargings) {

					String payMode = paySrcArray[i].toUpperCase();
					ExternalSystem system = productCharging.getExternalSystem();

					if (system.name().equalsIgnoreCase(payMode)) {
						if (!ExternalSystem.RS.equals(productCharging
								.getExternalSystem())) {
							paymentOptions.add(productCharging
									.getExternalSystem());
							break;
						}
					}

				}
			}

		}
		// Collections.sort(paymentOptions);
		return paymentOptions;
	}

	/**
	 * This method will return a list of chargings that are applicable for a
	 * specific subscriber In case of variable charging, the chargings
	 * associated with satisfied condition is returned
	 * 
	 * @param fdpRequest
	 * @param product
	 * @return
	 * @throws ExecutionFailedException
	 */
	private List<ProductCharging> getApplicableProductCharging(
			final FDPRequest fdpRequest, final Product product)
			throws ExecutionFailedException {
		List<ProductCharging> applicableProductChargings = product
				.getProductCharging(fdpRequest.getChannel(),
						ChargingType.NORMAL);
		for (ProductCharging productCharging : applicableProductChargings) {
			if (productCharging instanceof VariableCharging) {
				final VariableCharging variableCharging = (VariableCharging) productCharging;
				final VariableCharging newVariableCharging = new VariableCharging(
						variableCharging.getConditionStep(),
						variableCharging.getCommandDisplayName(),
						variableCharging.getExternalSystem());
				applicableProductChargings = newVariableCharging
						.getApplicableChargings(fdpRequest);
				break;
			}
		}
		return applicableProductChargings;
	}

	/**
	 * This method will prepare text for charging options
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public String getNotificationText(final FDPRequest fdpRequest,
			final List<ExternalSystem> paymentOptions) {
		StringBuilder notificationText = new StringBuilder("");
		final Map<ExternalSystem, String> paymentChoiceDisplayMap = getPaymentChoiceDisplayMap(fdpRequest);
		final Map<String, String> configurationMap = fdpRequest.getCircle()
				.getConfigurationKeyValueMap();
		String paymentChoiceHeaderText = configurationMap
				.get(ConfigurationKey.PAYMENT_CHOICE_TEXT.getAttributeName());
		notificationText
				.append((paymentChoiceHeaderText == null || paymentChoiceHeaderText
						.isEmpty()) ? "Choose payment mode\n"
						: paymentChoiceHeaderText + "\n");
		for (int i = 1; i <= paymentOptions.size(); i++) {
			notificationText.append(i + ". "
					+ paymentChoiceDisplayMap.get(paymentOptions.get(i - 1))
					+ "\n");
		}
		return notificationText.toString();
	}

	/**
	 * This method will return a map of all external system and their display
	 * names from configuration
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public Map<ExternalSystem, String> getPaymentChoiceDisplayMap(
			final FDPRequest fdpRequest) {
		final Map<ExternalSystem, String> paymentChoiceDisplayMap = new HashMap<ExternalSystem, String>();
		final Map<String, String> configurationMap = fdpRequest.getCircle()
				.getConfigurationKeyValueMap();
		String airDisplayText = configurationMap
				.get(ConfigurationKey.AIR_DISPLAY_TEXT.getAttributeName());
		paymentChoiceDisplayMap
				.put(ExternalSystem.AIR,
						((airDisplayText == null || airDisplayText.isEmpty()) ? "Main Account"
								: airDisplayText));
		String mobileMoneyDisplayText = configurationMap
				.get(ConfigurationKey.MOBILE_MONEY_DISPLAY_TEXT
						.getAttributeName());
		paymentChoiceDisplayMap.put(ExternalSystem.MM,
				((mobileMoneyDisplayText == null || mobileMoneyDisplayText
						.isEmpty()) ? "Mobile Money" : mobileMoneyDisplayText));
		String loyaltyDisplayText = configurationMap
				.get(ConfigurationKey.LOYALITY_DISPLAY_TEXT.getAttributeName());
		paymentChoiceDisplayMap
				.put(ExternalSystem.Loyalty,
						((loyaltyDisplayText == null || loyaltyDisplayText
								.isEmpty()) ? "Loyalty Points"
								: loyaltyDisplayText));
		String evdsDisplayText = configurationMap
				.get(ConfigurationKey.EVDS_DISPLAY_TEXT.getAttributeName());
		paymentChoiceDisplayMap
				.put(ExternalSystem.EVDS,
						((evdsDisplayText == null || evdsDisplayText.isEmpty()) ? "EVDS"
								: evdsDisplayText));
		return paymentChoiceDisplayMap;
	}

	@Override
	public boolean isRuleNoInputRequired() {
		return true;
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
