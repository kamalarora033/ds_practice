package com.ericsson.fdp.business.airrecharge;

import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This interface provides the method to execute air recharge.
 * 
 * @author Ericsson
 * 
 */
public interface AirRecharge {

	/**
	 * This method is used to execute air recharge.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @return the status of execution.
	 * @throws ExecutionFailedException
	 *             Exception if any.
	 */
	Status executeAirRecharge(FDPRequest fdpRequest) throws ExecutionFailedException;

}
