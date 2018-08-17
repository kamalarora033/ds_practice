package com.ericsson.fdp.business.step;

import java.io.Serializable;

import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;

/**
 * The interface defines steps that are executable.
 * 
 * @author Ericsson
 * 
 */
public interface FDPStep extends Serializable {

	/**
	 * The method executes the step.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @return True, if the step was successfully executed, false otherwise.
	 * @throws StepException
	 *             Exception, if occurs in executing the step.
	 */
	FDPStepResponse executeStep(FDPRequest fdpRequest) throws StepException;

	/**
	 * This method is used to get the step name.
	 * 
	 * @return the step name.
	 */
	String getStepName();
	
	
	/**
	 * Get wheather step is executed or not
	 * @return
	 */
	boolean isStepExecuted();

	/**
	 * set wheather step is executed or not
	 * @return
	 */
	void setStepExecuted(boolean stepexecuted);

	
}
