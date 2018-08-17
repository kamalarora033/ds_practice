package com.ericsson.fdp.business.charging.impl;

import com.ericsson.fdp.business.charging.FDPChargingSystem;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;

/**
 * This class defines the charging amount to be used in case of AIR.
 * 
 * @author Ericsson
 * 
 */
public class AIRCharging implements FDPChargingSystem<Long> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4393361275567557042L;
	/**
	 * The charging amount to be used.
	 */
	private Long chargingAmount;

	/**
	 * Instantiates a new aIR charging.
	 * 
	 * @param chargingAmount
	 *            the charging amount
	 */
	public AIRCharging(final Long chargingAmount) {
		this.chargingAmount = chargingAmount;
	}

	@Override
	public Long getChargingValue() {
		return chargingAmount;
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public String toString() {
		return "AIR" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + chargingAmount + FDPConstant.LOGGER_DELIMITER
				+ "AMTCHG" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + chargingAmount;
	}

	@Override
	public boolean getIsChargingRequired() {
		return chargingAmount != null && chargingAmount != 0;
	}

	@Override
	public ExternalSystem getChargingExternalSystem() {
		return ExternalSystem.AIR;
	}

	@Override
	public void setChargingValue(Object t) {
	chargingAmount=(Long)t;
		
	}

}