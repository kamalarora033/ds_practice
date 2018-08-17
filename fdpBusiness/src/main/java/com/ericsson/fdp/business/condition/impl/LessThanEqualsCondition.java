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
 * The class defines the less than equals condition.
 * 
 * @author Ericsson
 * 
 */
public class LessThanEqualsCondition extends AbstractCondition {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1648869069069570065L;
	/** The value of the right hand operand. */
	private CommandParamInput rightOperand = null;
	
	public CommandParamInput getRightHandOperand() {
		return rightOperand;
	}

	/**
	 * The constructor for the less than equals condition class.
	 * 
	 * @param rightOperandToSet
	 *            The right operand value to set.
	 */
	public LessThanEqualsCondition(final CommandParamInput rightOperandToSet) {
		super();
		if (rightOperandToSet == null) {
			throw new IllegalArgumentException("Found 'null' argument for condition");
		}
		super.setConditionType(FDPCommandParameterConditionEnum.IS_GREATER_THAN_EQUAL_TO);
		this.rightOperand = rightOperandToSet;
	}

	@Override
	public String toString() {
		return " less than or equals '" + rightOperand.toString(true) + "'";
	}

	@Override
	public boolean evaluateConditionValueForProvidedInput(Object leftOperandValue, Object[] rightOperandValues)
			throws ConditionFailedException {
		//System.out.println("in less than equals condition");
		if (rightOperandValues == null || rightOperandValues.length == 0) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		try {
			Object rightOperandToCheck = rightOperandValues[0];
			Class<?> classToConvert = getCommandParameterDataType().getClazz();
			Object leftOperandToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(leftOperandValue, classToConvert);
			rightOperandToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(rightOperandToCheck, classToConvert);
			if (rightOperandToCheck instanceof Comparable && leftOperandToCheck instanceof Comparable) {
				if (((Comparable) rightOperandToCheck).compareTo(leftOperandToCheck) >= 0) {
					return true;
				} else {
					return false;
				}
			} else if(leftOperandValue instanceof List<?>){
				List<?> leftOperandsList = (List<?>) leftOperandValue;
				for(Object left : leftOperandsList){
					left = ClassUtil.getPrimitiveValueReturnNotNullObject(left, classToConvert);
					if(((Comparable) rightOperandToCheck).compareTo((Comparable)left) >= 0){
						return true;
					}
				}
				return false;
			}else {
				throw new ConditionFailedException("The specified operands cannot be compared");
			}
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("The specified operands cannot be compared as they are not valid", e);
		}
	}

	@Override
	protected Object[] evaluateRightOperands(FDPRequest fdpRequest) throws ConditionFailedException {
		try {
			this.rightOperand.evaluateValue(fdpRequest);
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}
		Object[] rightOperands = { this.rightOperand.getValue() };
		return rightOperands;
	}

}
