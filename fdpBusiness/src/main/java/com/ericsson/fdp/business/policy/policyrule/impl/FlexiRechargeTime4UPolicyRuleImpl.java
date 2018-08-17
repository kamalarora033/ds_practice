package com.ericsson.fdp.business.policy.policyrule.impl;

import java.util.ArrayList;
import java.util.List;
import ch.qos.logback.classic.Logger;
import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.impl.VariableCharging;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ChargingTypeNotifications;
import com.ericsson.fdp.business.enums.PolicyStatus;
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
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.ChargingType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * @author EAMASAC
 *
 */
public class FlexiRechargeTime4UPolicyRuleImpl extends ProductPolicyRuleImpl{

	private static final long serialVersionUID = -8636438791614204867L;
	
	
	
	
	/* (non-Javadoc)
	 * @see com.ericsson.fdp.business.policy.policyrule.impl.ProductPolicyRuleImpl#displayRule(com.ericsson.fdp.core.request.FDPRequest)
	 */
	@Override
	public FDPResponse displayRule(FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		if(executeCurrentPolicyCondition(fdpRequest) && evaluateAdditionalPolicyRule(fdpRequest, ProductAdditionalInfoEnum.TIME4U_INPUT_VOUCHER_PIN)) {
			fdpResponse =new FDPResponseImpl(Status.SUCCESS, false, ResponseUtil.createResponseMessageInList(
					fdpRequest.getChannel(), getNotificationText(fdpRequest), TLVOptions.SESSION_CONTINUE));
		}
		return fdpResponse;
	}
	
	/* (non-Javadoc)
	 * @see com.ericsson.fdp.business.policy.policyrule.impl.ProductPolicyRuleImpl#validatePolicyRule(java.lang.Object, com.ericsson.fdp.core.request.FDPRequest, java.lang.Object[])
	 */
	@Override
	public FDPPolicyResponse validatePolicyRule(Object input, FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {
		FDPPolicyResponse response = new FDPPolicyResponseImpl(PolicyStatus.SUCCESS, null, null, true, null);
		if (input == null && condition != null) {
			response = new FDPPolicyResponseImpl(PolicyStatus.POLICY_VALUE_NOT_FOUND, null, null, false, null);
		}else{
			try {
				
				if(!PolicyRuleValidateImpl.isNullorEmpty(input) || !isVoucherCodeValid(fdpRequest, input.toString())){
					response = new FDPPolicyResponseImpl(PolicyStatus.FAILURE, "Invalid voucher code in the request", null, true,
							ResponseUtil.createResponseMessageInList(fdpRequest.getChannel(), "Invalid voucher code in the request",
									TLVOptions.SESSION_TERMINATE));
				}
			} catch (final Exception e) {
				throw new ExecutionFailedException("The policy rule could not be evaluated.", e);
			}
		}
		return response;
	}

	/**
	 * This method will validate the input voucher code check for the desired length and numeric characters
	 * @param beneficiaryMsisdn
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private boolean isVoucherCodeValid(FDPRequest fdpRequest, String voucherCode) throws ExecutionFailedException {
        Boolean isVoucherCodeValid = false;
        if(null != voucherCode && voucherCode.length() >0){
               ((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TIME4U_VOUCHER_ID, voucherCode);
               ((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TPIN, voucherCode);
               isVoucherCodeValid = true;
        }else{
               FDPLogger.info(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), getClass(), "isVoucherCodeValid", 
                            "Invalid voucher code in the request: " + voucherCode);
        }
        return isVoucherCodeValid;
 }


	@Override
	public boolean isRuleNoInputRequired() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.ericsson.fdp.business.policy.policyrule.impl.ProductPolicyRuleImpl#getNotificationText(com.ericsson.fdp.core.request.FDPRequest)
	 */
	protected String getNotificationText(FDPRequest fdpRequest){
		FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		String notificationText = null;
		if(fdpCacheable instanceof Product){
			Product product = (Product) fdpCacheable;
			/*if (ProductType.FlexiRecharge_Time4U.equals(product.getProductType())) {
				notificationText = policyRuleText;
			}*/
			final Long notificationId = product.getNotificationIdForChannel(fdpRequest.getChannel(),
					ChargingTypeNotifications.TIME4U_INPUT_VOUCHER_PIN);
			final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
			try {
				notificationText = TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger);
			} catch (NotificationFailedException e) {
				e.printStackTrace();
				notificationText = "Please enter the voucher pin:";
			}
	}
		return notificationText;
	}
	
	/**
	 * Policy Skip logic
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private boolean executeCurrentPolicyCondition(final FDPRequest fdpRequest) throws ExecutionFailedException {
		boolean execute = false;
		//Case when EVD Option is selected by User as Payment Option.
		if(ExternalSystem.EVDS.equals(fdpRequest.getExternalSystemToCharge())) {
			execute = true;
		}
		
		// Case when product is having Policy configuration -- true and EVD is not
		// configured as payment mode on product. Case when there is
		// configuration of Refill at Product Level for voucher-code.
		// commented to execute normal flow of product buy from USSD
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if(!execute && fdpCacheable instanceof Product) {
			Product product = (Product) fdpCacheable;
			final List<ExternalSystem> externalSystem = new ArrayList<>();
			final List<ProductCharging> productChargings = getApplicableProductCharging(fdpRequest, product);
			for (ProductCharging productCharging : productChargings) {
				externalSystem.add(productCharging.getExternalSystem());
			}
//			execute = externalSystem.contains(ExternalSystem.EVDS) ? false : true;
			
			execute=product.getProductType()==ProductType.Time4U?true:false;
				
		}
		
		return execute;
	}
	
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
}