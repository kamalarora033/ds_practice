package com.ericsson.fdp.business.expression.impl;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.expression.LeftOperand;
import com.ericsson.fdp.business.util.CommandParamUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class defines the value of the left operand that can be evaluated.
 * 
 * @author Ericsson
 * 
 */
public class FDPCommandLeftOperand implements LeftOperand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3071157486262929831L;
	/**
	 * The command param output for which the condition is to be levied.
	 */
	private CommandParamOutput fdpCommandParamOutput;

	public FDPCommandLeftOperand(final CommandParamOutput fdpCommandParamOutputToSet) {
		this.fdpCommandParamOutput = fdpCommandParamOutputToSet;
	}
	
	

	public CommandParamOutput getFdpCommandParamOutput() {
		return fdpCommandParamOutput;
	}



	@Override
	public Object evaluateValue(final FDPRequest fdpRequest) throws EvaluationFailedException {
		return CommandParamUtil.evaluateCommandParameter(fdpRequest, fdpCommandParamOutput);
	}
	
	/**
	 * Added by Rahul : This method call is to evaluate the combined value based on the 'Id' attributes
	 * @param fdpRequest
	 * @param combinedExpression
	 * @return
	 * @throws EvaluationFailedException
	 */
	public Object evaluateCombinedValue(final FDPRequest fdpRequest , Expression combinedExpression) throws EvaluationFailedException
	{
		return CommandParamUtil.evaluateCombinedCommandParameter(fdpRequest, fdpCommandParamOutput ,  combinedExpression);
	}

	@Override
	public String toString() {
		return fdpCommandParamOutput.getCommand().getCommandDisplayName() + FDPConstant.PARAMETER_SEPARATOR
				+ fdpCommandParamOutput.flattenParam();
	}

}
