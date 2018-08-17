package com.ericsson.fdp.business.condition.impl;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.condition.AbstractCondition;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.FDPCommandParameterConditionEnum;

/**
 * This class defines the equal condition.
 * 
 * @author Ericsson
 * 
 */
public class EqualsCondition extends AbstractCondition {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = -8581231977546865021L;
	/** The value of the right hand operand. */
	private CommandParamInput rightHandOperand = null;

	public CommandParamInput getRightHandOperand() {
		return rightHandOperand;
	}

	/**
	 * The constructor for the equals condition class.
	 * 
	 * @param rightHandOperandToSet
	 *            The right hand operator to set.
	 */
	public EqualsCondition(final CommandParamInput rightHandOperandToSet) {
		super();
		if (rightHandOperandToSet == null) {
			throw new IllegalArgumentException("Found 'null' argument for condition");
		}
		super.setConditionType(FDPCommandParameterConditionEnum.IS_EQUAL_TO);
		this.rightHandOperand = rightHandOperandToSet;
	}

	@Override
	public String toString() {
		return " equals '" + rightHandOperand.toString(true) + "'";
	}

	@Override
	public boolean evaluateConditionValueForProvidedInput(final Object leftOperandValue,
			final Object[] rightOperandValues) throws ConditionFailedException {
		//System.out.println("in equals condition");
		if (rightOperandValues == null || rightOperandValues.length == 0) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		try {
			Class<?> classToConvert = getCommandParameterDataType().getClazz();
			Object leftOperandToCheck = ClassUtil
					.getPrimitiveValueReturnNotNullObject(leftOperandValue, classToConvert);
			Object rightOperandToCheck = rightOperandValues[0];
			rightOperandToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(rightOperandToCheck, classToConvert);
			// Evaluate the Rule
			if (rightOperandToCheck instanceof String && leftOperandToCheck instanceof String) {
				String secondOperand = (String) leftOperandToCheck;
				if (secondOperand.length() == 0) {
					throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
				}
				if (rightOperandToCheck.toString().equalsIgnoreCase(secondOperand)) {
					return true;
				}
				return false;
			}
			if(leftOperandToCheck instanceof List<?>){
				//System.out.println("left operand is an instanceof list");
				List<?> leftOperandsList = (List<?>) leftOperandValue;
				for(Object left : leftOperandsList){
					left = ClassUtil.getPrimitiveValueReturnNotNullObject(left, classToConvert);
					if(rightOperandToCheck.toString().equals(left.toString())){
						return true;
					}
				}
				return false;
			}
			if (rightOperandToCheck instanceof Comparable && leftOperandToCheck instanceof Comparable) {
				if (((Comparable) rightOperandToCheck).compareTo(leftOperandToCheck) == 0) {
					return true;
				} else {
					return false;
				}
			} else {
				if (rightOperandToCheck.equals(leftOperandToCheck)) {
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
	protected Object[] evaluateRightOperands(final FDPRequest fdpRequest) throws ConditionFailedException {
		try {
			this.rightHandOperand.evaluateValue(fdpRequest);
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}
		Object[] rightOperands = { this.rightHandOperand.getValue() };
		return rightOperands;
	}

}
