package com.ericsson.fdp.business.fulfillment.service;

import javax.ejb.Remote;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This interface is used for service execution for the fulfillment services.
 * 
 * @author Ericsson
 * 
 */
@Remote
public interface FDPFulfillmentService {

	/**
	 * This method executes the service for the fullfillment request.
	 * 
	 * @param fdpRequest
	 * @param additionalInformations
	 * @return
	 * @throws ExecutionFailedException
	 */
	FDPResponse execute(final FDPRequest fdpRequest, final Object... additionalInformations) throws ExecutionFailedException;
	
}
