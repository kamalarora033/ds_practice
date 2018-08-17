package com.ericsson.fdp.business.step.execution.impl;

import java.util.List;

import com.ericsson.fdp.business.enums.EMAServiceMode;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * This interface is used to define the activation service.
 * 
 * @author Ericsson
 * 
 */
public interface ActivationService {

	/**
	 * This method is used to execute the activation commands.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @param fdpCommands
	 *            the commands to be executed.
	 * @param activationCommandMode
	 *            the mode of execution.
	 * @param emaServiceMode
	 *            the service mode of execution.
	 * @return the response of execution.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	FDPStepResponse executeActivationCommands(FDPRequest fdpRequest, List<FDPCommand> fdpCommands,
			ThreeGActivationCommandMode activationCommandMode, EMAServiceMode emaServiceMode)
			throws ExecutionFailedException;
}
