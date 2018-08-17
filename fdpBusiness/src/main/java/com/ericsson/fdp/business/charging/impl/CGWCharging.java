package com.ericsson.fdp.business.charging.impl;

import com.ericsson.fdp.business.charging.FDPChargingSystem;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;

/**
 * This class defines the service id to be used in case of CGW.
 * 
 * @author Ericsson
 * 
 */
public class CGWCharging implements FDPChargingSystem<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8316843636579033088L;

	/**
	 * The service id to be used.
	 */
	private String serviceId;

	/**
	 * The content type;
	 */
	private String contentType;

	/**
	 * Instantiates a new cGW charging.
	 * 
	 * @param serviceId
	 *            the service id
	 */
	public CGWCharging(final String serviceId, final String contentType) {
		this.serviceId = serviceId;
		this.contentType = contentType;
	}

	@Override
	public String getChargingValue() {
		return serviceId;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public String toString() {
		return "CGW" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + "Service_Id" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
				+ serviceId + FDPConstant.COMMA + "Content_Type" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + contentType;
	}

	@Override
	public boolean getIsChargingRequired() {
		return true;
	}

	@Override
	public ExternalSystem getChargingExternalSystem() {
		return ExternalSystem.CGW;
	}

	@Override
	public void setChargingValue(Object t) {
		// TODO Auto-generated method stub
		
	}
}
