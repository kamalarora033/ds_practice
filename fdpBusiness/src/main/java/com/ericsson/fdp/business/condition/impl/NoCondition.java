package com.ericsson.fdp.business.condition.impl;

import com.ericsson.fdp.business.condition.AbstractCondition;
import com.ericsson.fdp.common.exception.ConditionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.FDPCommandParameterConditionEnum;

/**
 * This class is used to implement the condition which always evaluates to true.
 * 
 * @author Ericsson
 * 
 */
public class NoCondition extends AbstractCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8988494440262092559L;

	/**
	 * The constructor for contains condition.
	 */
	public NoCondition() {
		super.setConditionType(FDPCommandParameterConditionEnum.NO_CONDITION);
	}

	@Override
	public String toString() {
		return "no condition";
	}

	@Override
	public boolean evaluateConditionValueForProvidedInput(final Object leftOperandValue,
			final Object[] rightOperandValues) throws ConditionFailedException {
		return true;
	}

	@Override
	protected Object[] evaluateRightOperands(final FDPRequest fdpRequest) throws ConditionFailedException {
		return null;
	}
}
