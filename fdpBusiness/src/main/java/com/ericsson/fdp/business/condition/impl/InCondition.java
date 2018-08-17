package com.ericsson.fdp.business.condition.impl;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.condition.AbstractCondition;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.FDPCommandParameterConditionEnum;

/**
 * This class is used to implement the contains condition.
 * 
 * @author Ericsson
 * 
 */
public class InCondition extends AbstractCondition {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 7209296394170740267L;
	/** The list of possible values for the right hand operator. */
	private CommandParamInput possibleValues = null;

	/**
	 * The constructor for contains condition.
	 * 
	 * @param possibleValuesToSet
	 *            The list of possible values.
	 */
	public InCondition(final CommandParamInput possibleValuesToSet) {
		super();
		if (possibleValuesToSet == null) {
			throw new IllegalArgumentException("Found 'null' or invalid type argument for Condition");
		}

		super.setConditionType(FDPCommandParameterConditionEnum.IN);
		this.possibleValues = possibleValuesToSet;
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(" in '");
		stringBuffer.append(possibleValues.toString(true)).append(FDPConstant.COMMA);
		return stringBuffer.toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean evaluateConditionValueForProvidedInput(final Object leftOperandValue,
			final Object[] rightOperandValues) throws ConditionFailedException {
		//System.out.println("in in condition");
		if (rightOperandValues == null || rightOperandValues.length == 0) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		if (rightOperandValues[0] instanceof List<?>) {
			List<Object> possibleValuesToUse = (List<Object>) rightOperandValues[0];
			// Evaluate the Rule
			if (possibleValuesToUse.get(FDPConstant.FIRST_INDEX) instanceof List<?>) {
				List<Object> values = (List<Object>) possibleValuesToUse.get(FDPConstant.FIRST_INDEX);
				if (values.get(FDPConstant.FIRST_INDEX) instanceof String && leftOperandValue instanceof String) {
					final String secondOperand = (String) leftOperandValue;
					if (secondOperand.length() == 0) {
						throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
					}
					for (Object object : values) {
						if (object.toString().equalsIgnoreCase(secondOperand)) {
							return true;
						}
					}
					return false;
				}
				if(leftOperandValue instanceof List<?>){
					//System.out.println("left operand is an instanceof list");
					List<?> leftOperandsList = (List<?>) leftOperandValue;
					for (Object object : possibleValuesToUse) {
						for(Object left : leftOperandsList){
							if(object.equals(left)){
								return true;
							}
						}
					}
					return false;
				}
				for (Object object : values) {
					if (object.equals(leftOperandValue)) {
						return true;
					}
				}
			}
		} else {
			throw new ConditionFailedException("Right operand value expected as list.");
		}
		return false;
	}

	@Override
	protected Object[] evaluateRightOperands(final FDPRequest fdpRequest) throws ConditionFailedException {
		List<Object> possibleValuesToSet = new ArrayList<Object>();
		try {
			possibleValues.evaluateValue(fdpRequest);
			possibleValuesToSet.add(possibleValues.getValue());
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}
		Object[] rightOperands = { possibleValuesToSet };
		return rightOperands;
	}
}
