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
 * This class is used to implement the Not-In condition.
 * 
 * @author Ericsson
 * 
 */
public class NotInCondition extends AbstractCondition {

	/** Serial Version UID . */
	private static final long serialVersionUID = 7209296394170740267L;

	/** The list of possible values for the right hand operator. */
	private List<CommandParamInput> possibleValues = null;

	/**
	 * The constructor for contains condition.
	 * 
	 * @param possibleValuesToSet
	 *            The list of possible values.
	 */
	public NotInCondition(final List<CommandParamInput> possibleValuesToSet) {
		super();
		if (possibleValuesToSet == null) {
			throw new IllegalArgumentException("Found 'null' or invalid type argument for Condition");
		}

		super.setConditionType(FDPCommandParameterConditionEnum.NOT_IN);
		this.possibleValues = possibleValuesToSet;
	}

	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(" Not IN '");
		for (CommandParamInput commandParamInput : possibleValues) {
			stringBuffer.append(commandParamInput.toString(true)).append(FDPConstant.COMMA);
		}
		stringBuffer.append("'");
		return stringBuffer.toString();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean evaluateConditionValueForProvidedInput(final Object leftOperandValue,
			final Object[] rightOperandValues) throws ConditionFailedException {
		
		//System.out.println("evaluateConditionValueForProvidedInput : Not In condition");
		boolean isValueNotExists = false;
		
		if (rightOperandValues == null || rightOperandValues.length == 0) {
			throw new ConditionFailedException("Right Operand value is null/ empty and cannot be processed.");
		}
		if (rightOperandValues[0] instanceof List<?>) {
			List<Object> possibleValuesToUse = (List<Object>) rightOperandValues[0];
			
			if (possibleValuesToUse.get(FDPConstant.FIRST_INDEX) instanceof List<?>) {		
				possibleValuesToUse = (List<Object>) possibleValuesToUse.get(FDPConstant.FIRST_INDEX);
			}
			
			if (possibleValuesToUse.get(FDPConstant.FIRST_INDEX) instanceof String && leftOperandValue instanceof String) {	
					
				final String secondOperand = (String) leftOperandValue;
				if (secondOperand.length() == 0) {
					throw new ConditionFailedException("Left Operand value is null/ empty and cannot be processed.");
				}
				for (Object object : possibleValuesToUse) {
					if (object.toString().equalsIgnoreCase(secondOperand)) {
						return isValueNotExists;
					}
				}
				isValueNotExists = true;
				
			} else if(leftOperandValue instanceof List<?>) {
					
				//System.out.println("left operand is an instanceof list");
				List<?> leftOperandsList = (List<?>) leftOperandValue;
				for (Object object : possibleValuesToUse) {
					for(Object left : leftOperandsList){
						if(object.toString().equals(left.toString())){
							return isValueNotExists;
						}
					}
				}
				isValueNotExists = true;
					
			} else {
				for (Object object : possibleValuesToUse) {
					if (object.toString().equals(leftOperandValue.toString())) {
						return isValueNotExists;
					}
				}
				isValueNotExists = true;
			}
		} else {
			throw new ConditionFailedException("Right operand value expected as list.");
		}
		return isValueNotExists;
	}

	@Override
	protected Object[] evaluateRightOperands(final FDPRequest fdpRequest) throws ConditionFailedException {
		List<Object> possibleValuesToSet = new ArrayList<Object>();
		try {
			for (CommandParamInput commandParamInput : this.possibleValues) {
				commandParamInput.evaluateValue(fdpRequest);
				possibleValuesToSet.add(commandParamInput.getValue());
			}
		} catch (EvaluationFailedException e) {
			throw new ConditionFailedException("Could not evaluate value ", e);
		}
		Object[] rightOperands = { possibleValuesToSet };
		return rightOperands;
	}
}
