package com.ericsson.fdp.business.ivr.responsegenerator;

import javax.ejb.Remote;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * This interface is to prepare the IVR XML Response.
 * 
 * @author Ericsson.
 * 
 */
@Remote
public interface IVRResponseGenerator {

	/**
	 * This method is used to generate the IVR XML response.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public String generateIVRResponse(final FDPRequest fdpRequest, final FDPResponse fdpResponse) throws ExecutionFailedException;
}
