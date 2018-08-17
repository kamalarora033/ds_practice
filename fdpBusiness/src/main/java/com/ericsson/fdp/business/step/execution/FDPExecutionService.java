package com.ericsson.fdp.business.step.execution;

import java.util.Map;

import javax.ejb.Remote;

import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * The execution service.
 * 
 * @author Ericsson
 * 
 */
@Remote
public interface FDPExecutionService {

	/**
	 * This method is used to execute a service.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @return the service status.
	 * @param additionalInformations
	 *            any additional information to be set.
	 * @exception ExecutionFailedException
	 *                Exception in execution.
	 */
	FDPStepResponse executeService(FDPRequest fdpRequest, Object... additionalInformations)
			throws ExecutionFailedException;

	FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation);

}
