package com.ericsson.fdp.business.decorator;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This interface is used to decorate the response.
 * 
 * @author Ericsson
 * 
 */
public interface FDPResponseDecorator {

	/**
	 * @return the decorated response object.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public FDPResponse decorateResponse() throws ExecutionFailedException;

}
