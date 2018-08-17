package com.ericsson.fdp.business.condition.impl;

import java.util.List;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.condition.AbstractCondition;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.FDPCommandParameterConditionEnum;

/**
 * This class defines the not equal condition.
 * 
 * @author Ericsson
 * 
 */
public class NotEqualsCondition extends AbstractCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2150289718064358685L;
	/**
	 * The serial version id.
	 */
	/** The value of the right hand operand. */
	private CommandParamInput rightHandOperand = null;

	/**
	 * The constructor for the equals condition class.
	 * 
	 * @param rightHandOperandToSet
	 *            The right hand operator to set.
	 */
	public NotEqualsCondition(final CommandParamInput rightHandOperandToSet) {
		super();
		if (rightHandOperandToSet == null) {
			throw new IllegalArgumentException("Found 'null' argument for condition");
		}
		super.setConditionType(FDPCommandParameterConditionEnum.NOT_EQUAL_TO);
		this.rightHandOperand = rightHandOperandToSet;
	}

	@Override
	public String toString() {
		return " not equals '" + rightHandOperand.toString(true) + "'";
	}

	@Override
	public boolean evaluateConditionValueForProvidedInput(Object leftOperandValue, Object[] rightOperandValues)
			throws ConditionFailedException {
		//System.out.println("in not equals");
		if (rightOperandValues == null || rightOperandValues.length == 0) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		try {
			Class<?> classToConvert = getCommandParameterDataType().getClazz();
			Object rightOperandToCheck = rightOperandValues[0];
			Object leftOperandToCheck = ClassUtil
					.getPrimitiveValueReturnNotNullObject(leftOperandValue, classToConvert);
			rightOperandToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(rightOperandToCheck, classToConvert);
			// Evaluate the Rule
			if (rightOperandToCheck instanceof String && leftOperandToCheck instanceof String) {
				String secondOperand = (String) leftOperandToCheck;
				if (secondOperand.length() == 0) {
					throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
				}
				if (!rightOperandToCheck.toString().equalsIgnoreCase(secondOperand)) {
					return true;
				}
				return false;
			}
			if(leftOperandToCheck instanceof List<?>){
				List<?> leftOperandsList = (List<?>) leftOperandValue;
				for(Object left : leftOperandsList){
					left = ClassUtil.getPrimitiveValueReturnNotNullObject(left, classToConvert);
					if(rightOperandToCheck.toString().equals(left.toString())){
						return false;
					}
				}
				return true;
			}
			if (rightOperandToCheck instanceof Comparable && leftOperandToCheck instanceof Comparable) {
				if (((Comparable) rightOperandToCheck).compareTo(leftOperandToCheck) != 0) {
					return true;
				} else {
					return false;
				}
			} else {
				if (!rightOperandToCheck.equals(leftOperandToCheck)) {
					return true;
				} else {
					return false;
				}
			}
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("The specified operands cannot be compared as they are not valid", e);
		}
	}

	@Override
	protected Object[] evaluateRightOperands(FDPRequest fdpRequest) throws ConditionFailedException {
		try {
			this.rightHandOperand.evaluateValue(fdpRequest);
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}
		Object[] rightOperands = { this.rightHandOperand.getValue() };
		return rightOperands;
	}

	public CommandParamInput getRightHandOperand() {
		return rightHandOperand;
	}
}
