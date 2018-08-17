package com.ericsson.fdp.business.serviceprovisioning;

import java.io.Serializable;

import javax.ejb.Remote;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;

/**
 * The interface defines the service provisioning.
 * 
 * @author Ericsson
 * 
 */
//@Remote
public interface ServiceProvisioning extends Serializable {

	/**
	 * The method executes the service provisioning flow.
	 * 
	 * @param fdpRequest
	 *            The input of the service provisioning.
	 * @return The response after executing service provisioning.
	 * @exception ExecutionFailedException
	 *                Exception, if the service provisioning flow could not be
	 *                completed.
	 */
	FDPResponse executeServiceProvisioning(FDPRequest fdpRequest) throws ExecutionFailedException;

}
