package com.ericsson.fdp.business.charging.value;

import com.ericsson.fdp.common.enums.ExternalSystem;

/**
 * This class implements the charging value.
 * 
 * @author Ericsson
 * 
 */
public class ChargingValueImpl implements ChargingValue {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4612362655483399075L;

	/**
	 * The charging value.
	 */
	private Object chargingValue;

	/**
	 * The charging Amount.
	 */
	private String chargingAmount;
	
	/**
	 * The content type.
	 */
	private String contentType;

	/**
	 * If charging is required flag.
	 */
	private boolean chargingRequired;

	/**
	 * The external system to use.
	 */
	private ExternalSystem externalSystemToUse;

	public ExternalSystem getExternalSystemToUse() {
		return externalSystemToUse;
	}

	/**
	 * @param externalSystemToUse the externalSystemToUse to set
	 */
	public void setExternalSystemToUse(ExternalSystem externalSystemToUse) {
		this.externalSystemToUse = externalSystemToUse;
	}

	@Override
	public Object getChargingValue() {
		return chargingValue;
	}

	/**
	 * @param chargingValue
	 *            the chargingValue to set
	 */
	public void setChargingValue(Object chargingValue) {
		this.chargingValue = chargingValue;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * @param contentType
	 *            the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public boolean isChargingRequired() {
		return chargingRequired;
	}

	/**
	 * @param chargingRequired
	 *            the chargingRequired to set
	 */
	public void setChargingRequired(boolean chargingRequired) {
		this.chargingRequired = chargingRequired;
	}
	/**
	 * @param chargingAmount the chargingAmount to set
	 */
	public void setChargingAmount(String chargingAmount) {
		this.chargingAmount = chargingAmount;
	}

	/* (non-Javadoc)
	 * @see com.ericsson.fdp.business.charging.value.ChargingValue#getChargingAmount()
	 */
	@Override
	public String getChargingAmount() {
		return this.chargingAmount;
	}


}
