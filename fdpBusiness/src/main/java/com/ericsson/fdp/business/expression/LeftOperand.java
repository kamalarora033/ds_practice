package com.ericsson.fdp.business.expression;

import java.io.Serializable;

import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class defines the value of the left operand that can be evaluated.
 * 
 * @author Ericsson
 * 
 */
public interface LeftOperand extends Serializable {

	/**
	 * This method evaluates the value of the left operand.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @return The value of the operand.
	 * @throws EvaluationFailedException
	 */
	Object evaluateValue(FDPRequest fdpRequest) throws EvaluationFailedException;

}
