package com.ericsson.fdp.business.charging;

import java.io.Serializable;

import com.ericsson.fdp.common.enums.ExternalSystem;

/**
 * This interface defines the method to fetch values based on the charging
 * systems that are present.
 * 
 * @author Ericsson
 * 
 * @param <T>
 *            The output parameter type for the charging value.
 */
public interface FDPChargingSystem<T> extends Serializable {

	/**
	 * The method is used to get the charging value.
	 * 
	 * @return The charging value.
	 */
	T getChargingValue();

	/**
	 * The method is used to get the charging value.
	 * 
	 * @return The charging value.
	 */
	void setChargingValue(Object object);
	
	/**
	 * This method is used to get the content type.
	 * 
	 * @return the content type.
	 */
	String getContentType();

	/**
	 * This method is used to check if charging is required or not.
	 * 
	 * @return true if charging required, false otherwise.
	 */
	boolean getIsChargingRequired();
	
	/**
	 * This method is used to get the external system on which to charge.
	 * 
	 * @return the external system on which to charge
	 */
	ExternalSystem getChargingExternalSystem();
}
