package com.ericsson.fdp.business.condition;

import java.util.Arrays;
import java.util.List;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.expression.LeftOperand;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandParameterDataType;
import com.ericsson.fdp.dao.enums.FDPCommandParameterConditionEnum;

/**
 * The class defines the condition that will be evaluated.
 * 
 * @author Ericsson
 * 
 */
public abstract class AbstractCondition implements Condition {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 2892646855033119684L;

	/** The condition type of the condition. */
	private FDPCommandParameterConditionEnum conditionType = null;

	private CommandParameterDataType commandParameterDataType;

	/**
	 * The method evaluates the condition with the left operand that is
	 * provided.
	 * 
	 * @param leftOperand
	 *            The left operand value.
	 * @param fdpRequest
	 *            the request object used to evaluate the right operand values.
	 * @return True, if the condition is satisfied, false otherwise.
	 * @throws ConditionFailedException
	 *             Exception, if the condition cannot be evaluated.
	 */
	@Override
	public boolean evaluate(Object leftOperand, FDPRequest fdpRequest, boolean evaluateLeftOperand)
			throws ConditionFailedException {
		if (leftOperand == null) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		Object leftOperandValue = leftOperand;
		try {
			if (evaluateLeftOperand) {
				if (leftOperand instanceof LeftOperand) {
					leftOperandValue = ((LeftOperand) leftOperand).evaluateValue(fdpRequest);
				} else if (leftOperand instanceof CommandParamInput) {
					CommandParamInput paramInputValue = (CommandParamInput) leftOperand;
					paramInputValue.evaluateValue(fdpRequest);
					leftOperandValue = paramInputValue.getValue();
				} else {
					throw new ConditionFailedException("Left operand cannot be evaluated.");
				}
			}
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value of left operand ", e);
		}
		Object[] rightOperandValues = evaluateRightOperands(fdpRequest);
		return evaluateConditionValueForProvidedInput(leftOperandValue, rightOperandValues);
	}
	
	public abstract boolean evaluateConditionValueForProvidedInput(Object leftOperandValue, Object[] rightOperandValues)
			throws ConditionFailedException;

	/**
	 * This method is used to evaluate the right operand values.
	 * 
	 * @param fdpRequest
	 *            the request object to use.
	 * @return the values of the right operands.
	 * @throws ConditionFailedException
	 *             Exception, if evaluation fails.
	 */
	protected abstract Object[] evaluateRightOperands(FDPRequest fdpRequest) throws ConditionFailedException;

	/**
	 * @return connditionType.
	 */
	public FDPCommandParameterConditionEnum getConditionType() {
		return conditionType;
	}

	/**
	 * @param conditionTypeToSet
	 *            The condition type to set.
	 */
	public void setConditionType(final FDPCommandParameterConditionEnum conditionTypeToSet) {
		this.conditionType = conditionTypeToSet;
	}

	/**
	 * The command parameter data type to set.
	 * 
	 * @param commandParameterDataTypeToSet
	 */
	public void setCommandParameterDataType(final CommandParameterDataType commandParameterDataTypeToSet) {
		this.commandParameterDataType = commandParameterDataTypeToSet;
	}

	/**
	 * 
	 * @return the command parameter data type to set.
	 */
	public CommandParameterDataType getCommandParameterDataType() {
		return commandParameterDataType;
	}

}
