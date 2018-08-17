package com.ericsson.fdp.business.expression.impl;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.expression.LeftOperand;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class defines the value of the left operand that can be evaluated.
 * 
 * @author Ericsson
 * 
 */
public class FDPRequestCommandLeftOperand implements LeftOperand {

	/**
	 * The serial Version UID.
	 */
	private static final long serialVersionUID = 2917852795582015622L;
	/**
	 * The command param output for which the condition is to be levied.
	 */
	private CommandParamInput commandParamInput;

	public FDPRequestCommandLeftOperand(final CommandParamInput commandParamInput) {
		this.commandParamInput = commandParamInput;
	}

	@Override
	public Object evaluateValue(final FDPRequest fdpRequest) throws EvaluationFailedException {
		commandParamInput.evaluateValue(fdpRequest);
		return commandParamInput.getValue();
	}

	@Override
	public String toString() {
		return "FDPRequestCommandLeftOperand [commandParamInput=" + commandParamInput + "]";
	}
}
