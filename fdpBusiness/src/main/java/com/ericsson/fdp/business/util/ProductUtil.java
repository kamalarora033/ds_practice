package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.List;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.impl.DABasedAirCharging;
import com.ericsson.fdp.business.charging.impl.FixedCharging;
import com.ericsson.fdp.business.charging.impl.RecurringCharging;
import com.ericsson.fdp.business.charging.impl.VariableCharging;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.ChargingType;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/*
Feature Name: AIR + MOBILE MONEY + LOYALITY Charging
Changes: Added ExternalSystem to each ProductCharging in getExecutableFDPProductCharging() method
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * This class deals with utility functions with product.
 * 
 * @author Ericsson
 * 
 */
public class ProductUtil {

	/**
	 * Instantiates a new product util.
	 */
	private ProductUtil() {

	}

	/**
	 * This method is used to update for product buy policy.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @param productId
	 *            productId
	 * @param spId
	 *            the spId
	 * @throws EvaluationFailedException
	 *             Exception if evaluation
	 * @throws ExecutionFailedException
	 *             Exception in execution
	 */
	public static void updateForProductBuyPolicy(final FDPRequest fdpRequest, final String productId, final String spId, final ChargingType chargingType)
			throws EvaluationFailedException, ExecutionFailedException {
		final Product product = RequestUtil.updateProductAndSPInRequest(fdpRequest, productId, spId);
		if (fdpRequest instanceof FDPRequestImpl) {
			((FDPRequestImpl) fdpRequest).addMetaValue(RequestMetaValuesKey.PRODUCT, product);
		}
		final List<ProductCharging> productCharging = product.getProductCharging(fdpRequest.getChannel(),
				chargingType);
		for (final ProductCharging productChargingStep : productCharging) {
			ChargingValueImpl chargingvalue=new ChargingValueImpl();
			chargingvalue.setExternalSystemToUse(productChargingStep.getExternalSystem());
			updateValuesInRequest(fdpRequest, chargingvalue,
					fdpRequest.getSubscriberNumber());
			// The first step is of air/cgw charging. The other is not required.
			break;
		}
	}

	/**
	 * This method is used to update values in the request.
	 * 
	 * @param fdpRequest
	 *            the request
	 * @param chargingValue
	 *            the charging value.
	 * @param subscriberNumber
	 *            the subscriber number.
	 */
	public static void updateValuesInRequest(final FDPRequest fdpRequest, final ChargingValue chargingValue,
			final Long subscriberNumber) {
		if (fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.setSubscriberNumberToCharge(subscriberNumber);
			fdpRequestImpl.addMetaValue(RequestMetaValuesKey.CHARGING_STEP, chargingValue);
		}
	}
	
	/**
	 * This method will create copy of charging steps
	 * @param productChargings
	 * @return
	 */
	public static List<ProductCharging> getExecutableFDPProductCharging(final List<ProductCharging> productChargings) {
		final List<ProductCharging> chargings = new ArrayList<ProductCharging>();
		if(null != productChargings){
			for(ProductCharging charging : productChargings) {
				if (charging instanceof DABasedAirCharging) {
					final DABasedAirCharging daBasedAirCharging = (DABasedAirCharging) charging;
					final DABasedAirCharging airCharging = new DABasedAirCharging(daBasedAirCharging.getChargingValue(), daBasedAirCharging.getCommandDisplayName(), daBasedAirCharging.getSubscriberNumberToCharge(), daBasedAirCharging.getChargingDetails(), daBasedAirCharging.isPartialChargingAllowed(), daBasedAirCharging.getExternalSystem(), daBasedAirCharging.getDiscountId());
					chargings.add(airCharging);
				} else if(charging instanceof FixedCharging) {
					final FixedCharging fixedCharging = (FixedCharging) charging;
					final FixedCharging newFixedCharging = new FixedCharging(fixedCharging.getChargingValue(), fixedCharging.getCommandDisplayName(), fixedCharging.getSubscriberNumberToCharge(), fixedCharging.getExternalSystem(), fixedCharging.getDiscountId());
					chargings.add(newFixedCharging);
				} else if (charging instanceof RecurringCharging) {
					final RecurringCharging recurringCharging = (RecurringCharging) charging;
					final RecurringCharging newRecurringCharging = new RecurringCharging(recurringCharging.getRsParameters(), recurringCharging.getCommandDisplayName(), recurringCharging.getRsCharginAmt(), recurringCharging.getExternalSystem());
					chargings.add(newRecurringCharging);
				} else if (charging instanceof VariableCharging) {
					final VariableCharging variableCharging = (VariableCharging) charging;
					final VariableCharging newVariableCharging = new VariableCharging(variableCharging.getConditionStep(), variableCharging.getCommandDisplayName(), variableCharging.getExternalSystem());
					chargings.add(newVariableCharging);
				}
			}
		}
		return chargings;
	}
	
	/**
	 * This method will get all policy Identifiers.
	 * 
	 * @param product
	 * @return
	 */
	public static List<String> getPolicyIdentifierList(final Product product) {
		final List<String> list = new ArrayList<>();
		for(final ProductAdditionalInfoEnum additionalInfoEnum : ProductAdditionalInfoEnum.values()) {
			//System.out.println("ProductUtil.getPolicyIdentifierList : "+additionalInfoEnum+"="+product.getAdditionalInfo(additionalInfoEnum));
			//if(additionalInfoEnum.getAdditionalNotifications() && Boolean.valueOf(product.getAdditionalInfo(additionalInfoEnum))) {
			if(additionalInfoEnum.getAdditionalNotifications() && null != product.getAdditionalInfo(additionalInfoEnum)) {
				list.add(additionalInfoEnum.name());
			}
		}
		return list;
	}
	
	
	/**
	 * This method check if charging requested  for node.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public static void setSkipChargingForBalanceEnquiry(final FDPRequest fdpRequest) {		
		Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		try {
			if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof FDPNode) {
				final FDPNode fdpNode = (FDPNode) fdpRequest
						.getValueFromRequest(RequestMetaValuesKey.NODE);
				if (fdpNode instanceof ProductNode) {
					final ProductNode productNode = (ProductNode) fdpNode;
					if (FDPServiceProvSubType.BALANCE_ENQUIRY.equals(productNode.getServiceProvSubType()))
					{
                        FDPLogger.debug(circleLogger, ProductUtil.class, "setSkipChargingForBalanceEnquiry()",
                                LoggerUtil.getRequestAppender(fdpRequest) + "Skiping charging as it is of balance enquiry type");
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING, Boolean.TRUE);
					}				
							
				}
			}
		}catch(Exception e){
			//e.printStackTrace();
			FDPLogger.error(circleLogger, ProductUtil.class, "setSkipChargingForBalanceEnquiry()", LoggerUtil.getRequestAppender(fdpRequest)+"Error while checking for skip charging");
		}
		
	}
}
