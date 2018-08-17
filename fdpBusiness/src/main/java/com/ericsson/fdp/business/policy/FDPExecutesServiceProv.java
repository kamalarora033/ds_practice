/**
 * 
 */
package com.ericsson.fdp.business.policy;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * The Interface FDPExecutesServiceProv.
 * 
 * @author Ericsson
 */
public interface FDPExecutesServiceProv {

	/**
	 * Execute service prov.
	 * 
	 * @param request
	 *            the request
	 * @return the fDP response
	 */
	FDPResponse executeServiceProv(FDPRequest request) throws ExecutionFailedException;
}
