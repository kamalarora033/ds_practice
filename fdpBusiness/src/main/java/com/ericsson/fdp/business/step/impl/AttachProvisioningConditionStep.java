package com.ericsson.fdp.business.step.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.charging.impl.ExpressionCondition;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/**
 * This class defines the attach provisioning condition step.
 * 
 * @author Ericsson
 * 
 */
public class AttachProvisioningConditionStep implements FDPRollbackable, FDPStep {

	/**
	 *
	 */
	private static final long serialVersionUID = -4818967817594661170L;

	/** The expression which corresponds to the condition step. */
	private final Expression expression;

	/** The commands that have to be executed if the expression satisfies. */
	private final List<FDPStep> steps;

	/**
	 * The step name.
	 */
	private final String stepName;

	/**
	 * The step id.
	 */
	private final Long stepId;

	int stepFailed;

	private boolean executed;

	/**
	 * The constructor for attach provisioning step.
	 * 
	 * @param expressionToSet
	 *            The expression to set.
	 * @param commandsToSet
	 *            The command to set.
	 * @param stepId
	 *            the step id
	 * @param stepName
	 *            the step name
	 */
	public AttachProvisioningConditionStep(final Expression expressionToSet, final List<FDPStep> steps,
			final Long stepId, final String stepName) {
		this.expression = expressionToSet;
		this.steps = steps;
		this.stepId = stepId;
		this.stepName = stepName;
	}

	@Override
	public boolean performRollback(final FDPRequest fdpRequest) throws RollbackException {
		if (steps != null) {
			for (int i = stepFailed - 1; i >= 0; i--) {
				FDPStep stepExecuted = steps.get(i);
				if (stepExecuted instanceof FDPRollbackable) {
					if (!((FDPRollbackable) stepExecuted).performRollback(fdpRequest)) {
						throw new RollbackException("Could not perform rollback for step " + stepExecuted);
					}
				}
			}
		}
		return true;
	}

	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		boolean stepExecuted = false;
		boolean commandFailed = false;
		FDPStepResponse stepResponse = null;
		boolean isAsync = false;
		CommandStep commandStep = null;
		try {
			FDPLogger.debug(circleLogger, getClass(), "executeStep()", LoggerUtil.getRequestAppender(fdpRequest) + "Evaluating expression " + expression);
			// If the expression is satisfied, execute all commands.
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.VALIDATED_EXPRESSIONS_REQUESTER, new ArrayList<ExpressionCondition>());
			((FDPRequestImpl)fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.VALIDATED_EXPRESSIONS_BENEFICIARY, new ArrayList<ExpressionCondition>());
			if (expression == null || expression.evaluateExpression(fdpRequest)) {
				stepExecuted = true;
				if (steps != null) {
					for (final FDPStep fdpStep : steps) {
						commandStep = getCommandTypeStep(fdpStep);
						isAsync = (null != commandStep && CommandUtil.isAyncCommand(fdpRequest, commandStep.getCommandDisplayName())) ? true : false;
						stepResponse = fdpStep.executeStep(fdpRequest);
						boolean executionStatus = RequestUtil.checkExecutionStatus(stepResponse);
						if (!executionStatus) {
							stepExecuted = false;
							commandFailed = true;
							break;
						} else if (executionStatus && isAsync) {
							stepFailed++;
							break;
						}
						stepFailed++;
					}
				}
			}
		} catch (final ExpressionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "executeStep()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The requested step could not be processed as expression could not be processed.", e);
			throw new StepException("The requested step could not be processed as expression could not be processed.",
					e);
		}
		FDPLogger.debug(circleLogger, getClass(), "executeStep()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Step value found as :- " + stepExecuted);
		//return RequestUtil.createStepResponse(stepExecuted, commandFailed, stepResponse);
		return RequestUtil.createStepResponse(stepExecuted, commandFailed, stepResponse,isAsync);
	}

	@Override
	public String toString() {
		return " attach provisioning condition step. Expression :- " + expression + " steps :- " + steps;
	}

	@Override
	public String getStepName() {
		return stepName;
	}

	/**
	 * This method is used to get the step id.
	 * 
	 * @return the step id.
	 */
	public Long getStepId() {
		return stepId;
	}

	public List<FDPStep> getSteps() {
		return steps;
	}
	
	
	@Override
	public boolean isStepExecuted() {
		return executed;
	}


	@Override
	public void setStepExecuted(boolean stepexecuted) {
		this.executed=stepexecuted;
	}

	/**
	 * CHeck if type command step.
	 * @param fdpStep
	 * @return
	 */
	private CommandStep getCommandTypeStep(final FDPStep fdpStep) {
		CommandStep commandStep = null;
		if(fdpStep instanceof CommandStep) {
			commandStep = (CommandStep) fdpStep;
		}
		return commandStep;
	}
}
