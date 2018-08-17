package com.ericsson.fdp.business.condition;

import java.io.Serializable;

import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This interface defines the condition.
 * 
 * @author Ericsson
 * 
 */
public interface Condition extends Serializable {
	/**
	 * This method is used to evaluate a condition. This method evaluates the
	 * values of the right operand with respect to the provided request. It may
	 * evaluate the left operand value as specified by the boolean. It then
	 * proceeds to evaluate the condition.
	 * 
	 * @param leftOperand
	 *            the left operand to be used.
	 * @param fdpRequest
	 *            the request object used to evaluate the right operand values.
	 * @param evaluateLeftOperand
	 *            Evaluates the left operand value.
	 * @return true, if the evaluation is true, false otherwise.
	 * @throws ConditionFailedException
	 *             Exception if Condition fails.
	 */
	boolean evaluate(Object leftOperand, FDPRequest fdpRequest, boolean evaluateLeftOperand)
			throws ConditionFailedException;

	/**
	 * This method is used to evaluate the condition based on the provided input
	 * values. THe left operand value denotes the object to be used as left
	 * operand, the right operand value denotes the values to be used in the
	 * right operand.
	 * 
	 * @param leftOperandValue
	 *            the value of the left operand.
	 * @param rightOperandValues
	 *            the values of the right operands.
	 * @return true, if evaluation is true, false otherwise.
	 * @throws ConditionFailedException
	 *             Exception, if evaluation fails.
	 */
	boolean evaluateConditionValueForProvidedInput(Object leftOperandValue, Object[] rightOperandValues)
			throws ConditionFailedException;
}
