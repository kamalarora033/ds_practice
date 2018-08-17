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
 * The class defines the range condition.
 * 
 * @author Ericsson
 * 
 */
public class RangeCondition extends AbstractCondition {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 145392446544888604L;
	/** The minimum value of the range. */
	private CommandParamInput minValue = null;
	/** The maximum value of the range. */
	private CommandParamInput maxValue = null;

	/**
	 * The constructor for the range class.
	 * 
	 * @param minValueToSet
	 *            The minimum value to set.
	 * @param maxValueToSet
	 *            The maximum value to set.
	 */
	public RangeCondition(final CommandParamInput minValueToSet, final CommandParamInput maxValueToSet) {
		super();
		if (minValueToSet == null || maxValueToSet == null) {
			throw new IllegalArgumentException("Found 'null' argument for condition");
		}
		super.setConditionType(FDPCommandParameterConditionEnum.BETWEEN);
		this.minValue = minValueToSet;
		this.maxValue = maxValueToSet;
	}

	@Override
	public String toString() {
		return " range in  '" + minValue.toString(true) + "' to '" + maxValue.toString(true) + "'";
	}

	@Override
	public boolean evaluateConditionValueForProvidedInput(Object leftOperandValue, Object[] rightOperandValues)
			throws ConditionFailedException {
		//System.out.println("in range condition");
		if (rightOperandValues == null || rightOperandValues.length < 2) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		try {
			Class<?> classToConvert = getCommandParameterDataType().getClazz();
			Object leftOperandToCheck = ClassUtil
					.getPrimitiveValueReturnNotNullObject(leftOperandValue, classToConvert);
			Object minValueToCheck = rightOperandValues[0];
			Object maxValueToCheck = rightOperandValues[1];
			minValueToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(minValueToCheck, classToConvert);
			maxValueToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(maxValueToCheck, classToConvert);
			if (minValueToCheck instanceof Comparable && maxValueToCheck instanceof Comparable
					&& leftOperandToCheck instanceof Comparable) {
				if (((Comparable) minValueToCheck).compareTo(leftOperandToCheck) <= 0
						&& ((Comparable) maxValueToCheck).compareTo(leftOperandToCheck) >= 0) {
					return true;
				} else {
					return false;
				}
			} else if(leftOperandValue instanceof List<?>){
				List<?> leftOperandsList = (List<?>) leftOperandValue;
				for(Object left : leftOperandsList){
					left = ClassUtil.getPrimitiveValueReturnNotNullObject(left, classToConvert);
					if(((Comparable) minValueToCheck).compareTo((Comparable)left) < 0
							&& ((Comparable) maxValueToCheck).compareTo((Comparable)left) > 0){
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
			this.minValue.evaluateValue(fdpRequest);
			this.maxValue.evaluateValue(fdpRequest);
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}
		Object[] rightOperands = { this.minValue.getValue(), this.maxValue.getValue() };
		return rightOperands;
	}
}
