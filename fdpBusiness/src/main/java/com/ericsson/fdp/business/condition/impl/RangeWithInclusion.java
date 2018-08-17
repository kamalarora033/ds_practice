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
 * The class defines the range condition.
 * 
 * @author Ericsson
 * 
 */
public class RangeWithInclusion extends AbstractCondition {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = -71784879700327080L;
	/** The minimum value of the range. */
	private CommandParamInput minValue = null;
	/** The maximum value of the range. */
	private CommandParamInput maxValue = null;

	private List<CommandParamInput> includedValues;

	/**
	 * The constructor for the range class.
	 * 
	 * @param minValueToSet
	 *            The minimum value to set.
	 * @param maxValueToSet
	 *            The maximum value to set.
	 */
	public RangeWithInclusion(final CommandParamInput minValueToSet, final CommandParamInput maxValueToSet,
			final List<CommandParamInput> includedValuesToSet) {
		super();
		if (minValueToSet == null || maxValueToSet == null || includedValuesToSet == null) {
			throw new IllegalArgumentException("Found 'null' argument for condition");
		}
		super.setConditionType(FDPCommandParameterConditionEnum.BETWEEN_WITH_INCLUSION);
		this.minValue = minValueToSet;
		this.maxValue = maxValueToSet;
	}

	@Override
	public String toString() {
		return " range in  '" + minValue.toString(true) + "' to '" + maxValue.toString(true) + "'";
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean evaluateConditionValueForProvidedInput(Object leftOperandValue, Object[] rightOperandValues)
			throws ConditionFailedException {
		//System.out.println("in range inclusion condition");
		if (rightOperandValues == null || rightOperandValues.length < 3) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		if (rightOperandValues[2] instanceof List<?>) {
			try {
				Class<?> classToConvert = getCommandParameterDataType().getClazz();
				Object minValueToCheck = rightOperandValues[0];
				Object maxValueToCheck = rightOperandValues[1];
				Object leftOperandToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(leftOperandValue,
						classToConvert);
				List<Object> includedValuesToCheck = (List<Object>) rightOperandValues[2];
				minValueToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(minValueToCheck, classToConvert);
				maxValueToCheck = ClassUtil.getPrimitiveValueReturnNotNullObject(maxValueToCheck, classToConvert);
				if (minValueToCheck instanceof Comparable && maxValueToCheck instanceof Comparable
						&& leftOperandToCheck instanceof Comparable) {
					if (((Comparable) minValueToCheck).compareTo(leftOperandToCheck) < 0
							&& ((Comparable) maxValueToCheck).compareTo(leftOperandToCheck) > 0) {
						return true;
					} else if (includedValuesToCheck.contains(leftOperandToCheck)) {
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
						}else if(includedValuesToCheck.contains((Comparable)left)){
							return true;
						}else{
							return false;
						}
					}
					return false;
				}else {
					throw new ConditionFailedException("The specified operands cannot be compared");
				}
			} catch (EvaluationFailedException e) {
				throw new ConditionFailedException("The specified operands cannot be compared as they are not valid", e);
			}
		} else {
			throw new ConditionFailedException("Right operand value expected as list.");
		}
	}

	@Override
	protected Object[] evaluateRightOperands(FDPRequest fdpRequest) throws ConditionFailedException {
		List<Object> possibleValuesToSet = new ArrayList<Object>();
		try {
			for (CommandParamInput commandParamInput : this.includedValues) {
				commandParamInput.evaluateValue(fdpRequest);
				possibleValuesToSet.add(commandParamInput.getValue());
			}
			this.minValue.evaluateValue(fdpRequest);
			this.maxValue.evaluateValue(fdpRequest);
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}

		Object[] rightOperands = { this.minValue.getValue(), this.maxValue.getValue(), possibleValuesToSet };
		return rightOperands;
	}
}
