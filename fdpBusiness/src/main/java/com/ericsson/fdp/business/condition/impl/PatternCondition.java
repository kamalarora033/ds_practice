package com.ericsson.fdp.business.condition.impl;

import java.util.List;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.condition.AbstractCondition;
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
public class PatternCondition extends AbstractCondition {

	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 6630863916669627232L;
	/** The list of possible values for the right hand operator. */
	private CommandParamInput pattern;

	/**
	 * The constructor for contains condition.
	 * 
	 * @param possibleValuesToSet
	 *            The list of possible values.
	 */
	public PatternCondition(final CommandParamInput patternToSet) {
		super();
		if (patternToSet == null) {
			throw new IllegalArgumentException("Found 'null' or invalid type argument for Condition");
		}

		super.setConditionType(FDPCommandParameterConditionEnum.PATTERN);
		this.pattern = patternToSet;
	}

	@Override
	public String toString() {
		return " pattern '" + pattern.toString(true) + "'";
	}

	@Override
	public boolean evaluateConditionValueForProvidedInput(Object leftOperandValue, Object[] rightOperandValues)
			throws ConditionFailedException {
		//System.out.println("in pattern matches condition");
		if (rightOperandValues == null || rightOperandValues.length == 0) {
			throw new ConditionFailedException("Operand value is null/ empty and cannot be processed.");
		}
		Object rightOperandToCheck = rightOperandValues[0];
		// Evaluate the Rule
		if (leftOperandValue instanceof String && rightOperandToCheck instanceof String) {
			String leftOpStr = (String) leftOperandValue;
			return leftOpStr.matches((String) rightOperandToCheck);
		}else {
			if(leftOperandValue instanceof List<?>){
				List<?> leftOperandsList = (List<?>) leftOperandValue;
				for(Object left : leftOperandsList){
					if(left.toString().matches((String) rightOperandToCheck.toString())){
						return true;
					}
					return false;
				}
			}
			return leftOperandValue.toString().matches(rightOperandToCheck.toString());
		}
	}

	@Override
	protected Object[] evaluateRightOperands(FDPRequest fdpRequest) throws ConditionFailedException {
		try {
			this.pattern.evaluateValue(fdpRequest);
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}
		Object[] rightOperands = { this.pattern.getValue() };
		return rightOperands;
	}
}
