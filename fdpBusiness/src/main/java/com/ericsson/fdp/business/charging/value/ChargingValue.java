package com.ericsson.fdp.business.charging.value;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.common.enums.ExternalSystem;

/**
 * This interface defines the charging value.
 * 
 * @author Ericsson
 * 
 */
public interface ChargingValue extends FDPCacheable {

	/**
	 * This method is used to get the charging value to be used for the product.
	 * 
	 * @return The charging value.
	 */
	Object getChargingValue();
	
	/**
	 * @returnThis method is used to get the charging Amount to be used for the product. 
	 */
	public String getChargingAmount();
	/**

	/**
	 * This method is used to get the content type to be used for the product.
	 * 
	 * @return The charging value.
	 */
	String getContentType();

	/**
	 * This method defines if charging is required or not.
	 * 
	 * @return true if charging required false otherwise.
	 */
	boolean isChargingRequired();

	/**
	 * This method is used to get the external system to use.
	 * 
	 * @return the externalSystemToUse
	 */
	public ExternalSystem getExternalSystemToUse();

}
