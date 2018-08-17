package com.ericsson.fdp.business.charging.impl;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.charging.AbstractCharging;
import com.ericsson.fdp.business.charging.FDPChargingSystem;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ServiceProvisioningUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvProductDTO;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

/*
Feature Name: Discounted Charging + Overriding product cost in case COST parameter for product is available in URL
Changes: Added the discount in product cost
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * This class defines the values to be used in case of fixed charging.
 * 
 * @author Ericsson
 * 
 */
public class FixedCharging extends AbstractCharging {

	/**
	 * 
	 */
	private static final long serialVersionUID = -84795894761882873L;
	/**
	 * The charging value to be used.
	 */
	protected final FDPChargingSystem<? extends Object> chargingValue;

	protected ChargingValueImpl chargingValueImpl;

	/**
	 * Instantiates a new fixed charging.
	 * 
	 * @param chargingValue
	 *            the charging value
	 */
	public FixedCharging(final FDPChargingSystem<? extends Object> chargingValue, final String commandDisplayName,
			CommandParamInput subscriberNumberToCharge) {
		this.chargingValue = chargingValue;
		super.setCommandDisplayName(commandDisplayName);
		super.setSubscriberNumberToCharge(subscriberNumberToCharge);
	}
	
	public FixedCharging(final FDPChargingSystem<? extends Object> chargingValue, final String commandDisplayName,
			CommandParamInput subscriberNumberToCharge, final ExternalSystem externalSystem) {
		this(chargingValue, commandDisplayName, subscriberNumberToCharge);
		this.externalSystem = externalSystem;
	}
	
	public FixedCharging(final FDPChargingSystem<? extends Object> chargingValue, final String commandDisplayName,
			CommandParamInput subscriberNumberToCharge, final ExternalSystem externalSystem, final Long discountId) {
		this(chargingValue, commandDisplayName, subscriberNumberToCharge, externalSystem);
		this.discountId = discountId;
	}

	@Override
	public ChargingValue evaluateParameterValue(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (chargingValueImpl == null) {
			chargingValueImpl = new ChargingValueImpl();

			String spSubType = null;
			Object spObject = fdpRequest.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
			if(null != spObject) {
				ServiceProvisioningRule spRule = (ServiceProvisioningRule)spObject;
				ServiceProvProductDTO serviceProvProductDTO =  (ServiceProvProductDTO) spRule.getServiceProvDTO();
				spSubType =serviceProvProductDTO.getSpSubType().toString();
			}
			
			chargingValue.setChargingValue(getApplicableChargingAmount(fdpRequest));
			chargingValueImpl.setChargingValue(chargingValue.getChargingValue());
			chargingValueImpl.setContentType(chargingValue.getContentType());
			chargingValueImpl.setChargingRequired(chargingValue.getIsChargingRequired());
			chargingValueImpl.setExternalSystemToUse(chargingValue.getChargingExternalSystem());
			
			if(null!=spSubType && spSubType.equalsIgnoreCase(FDPConstant.ME2U_PREFIX))
				chargingValueImpl.setChargingRequired(false);
			
		}
		return chargingValueImpl;
	}

	/**
	 * @return the chargingValue
	 */
	public FDPChargingSystem<? extends Object> getChargingValue() {
		return chargingValue;
	}
	
	/**
	 * This method updates the chargingAmount based upon certain considerations
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public Object getApplicableChargingAmount(final FDPRequest fdpRequest) throws ExecutionFailedException{
		Long applicableCharging = Long.valueOf(this.chargingValue.getChargingValue().toString());
		FDPServiceProvSubType serviceProvSubType = ServiceProvisioningUtil.getFDPServiceProvSubType(fdpRequest);
		serviceProvSubType= serviceProvSubType==null ? ServiceProvisioningUtil.getFDPServiceProvSubTypeBySP(fdpRequest):serviceProvSubType;
		if(FDPServiceProvSubType.PRODUCT_BUY.equals(serviceProvSubType) || FDPServiceProvSubType.PRODUCT_BUY_RECURRING.equals(serviceProvSubType)
				|| FDPServiceProvSubType.PRODUCT_BUY_SPLIT.equals(serviceProvSubType) || FDPServiceProvSubType.RS_DEPROVISION_PRODUCT.equals(serviceProvSubType)){
			/* If product cost parameter is set, then override the product charging value */
			if(!ChannelType.RS.equals(fdpRequest.getChannel())){
				Object productCost = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.PRODUCT_COST);
				if(productCost != null){
					applicableCharging = -1 * Long.valueOf(productCost.toString());
					}
				applicableCharging = ChargingUtil.updateChargingIfSplit(fdpRequest, applicableCharging);
			}
			applicableCharging = getDiscountedChargingValue(fdpRequest, applicableCharging);
		}
		return applicableCharging;
	}
	
	
	/**
	 * This method return the discounted value, if discount is applicable
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public Long getDiscountedChargingValue(final FDPRequest fdpRequest, Long chargingValue) throws ExecutionFailedException{
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		try {
			chargingDiscount = getDiscountFromCache(fdpRequest);
		 	if(null != chargingDiscount && chargingDiscount.isDiscountApplicable(fdpRequest)){
		 		chargingValue = (-1 * chargingDiscount.calculateDiscount(chargingValue >= 0 ? chargingValue : (-1 * chargingValue)));
				FDPLogger.info(circleLogger, getClass(), "getDiscountedChargingValue()", LoggerUtil.getRequestAppender(fdpRequest) + "DISCOUNTED PRICE" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + chargingValue);
			}			
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Error Number Format Exception while calculating discount", e);
		} catch (ExpressionFailedException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Error ExpressionFailedException while calculating discount", e);
		}
		return chargingValue;
	}
}