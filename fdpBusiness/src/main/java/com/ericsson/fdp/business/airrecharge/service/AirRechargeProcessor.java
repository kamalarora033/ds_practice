package com.ericsson.fdp.business.airrecharge.service;

import com.ericsson.fdp.business.enums.OperatingMode;
import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * This interface is used to provide the implementation for the air recharge.
 * 
 * @author Ericsson
 * 
 */
public interface AirRechargeProcessor {

	/**
	 * This method is used to execute the air recharge.
	 * 
	 * @param inputXML
	 *            The input xml.
	 * @return The output xml.
	 * @exception ExecutionFailedException
	 *                Exception, if air recharge fails.
	 */
	String executeAirRecharge(String inputXML, String requestId, OperatingMode operatingMode, String incomingIpAddress)
			throws ExecutionFailedException;

}
