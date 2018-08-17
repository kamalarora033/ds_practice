package com.ericsson.fdp.business.step.impl;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The class defines the command step.
 *
 * @author Ericsson
 *
 */
public class CommandStep implements FDPStep, FDPRollbackable {

	/**
	 *
	 */
	private static final long serialVersionUID = 4058215488133655118L;

	private boolean executed;
	
	/** The command corresponding to this step. */
	protected FDPCommand fdpCommand;

	/** The command display name. */
	protected final String commandDisplayName;

	/**
	 * The step name.
	 */
	private String stepName;

	/**
	 * The step id.
	 */
	private final Long stepId;

	/**
	 * The constructor for command step.
	 *
	 * @param commandDisplayNameToSet
	 *            The command display name to set.
	 * @param stepId
	 *            the step id
	 * @param stepName
	 *            the step name
	 */
	public CommandStep(final String commandDisplayNameToSet, final Long stepId, final String stepName) {
		this.commandDisplayName = commandDisplayNameToSet;
		this.stepId = stepId;
	}

	/**
	 * Instantiates a new command step.
	 *
	 * @param fdpCommand
	 *            the fdp command
	 * @param commandDisplayNameToSet
	 *            the command display name to set
	 * @param stepId
	 *            the step id
	 * @param stepName
	 *            the step name
	 */
	public CommandStep(final FDPCommand fdpCommand, final String commandDisplayNameToSet, final Long stepId,
			final String stepName) {
		this.fdpCommand = fdpCommand;
		this.commandDisplayName = commandDisplayNameToSet;
		this.stepId = stepId;
		this.stepName = stepName;
	}

	@Override
	public boolean performRollback(final FDPRequest fdpRequest) throws RollbackException {
		boolean rollbackPerformed = true;
		if (fdpCommand instanceof FDPRollbackable) {
			if(fdpCommand.getSystem().equals(ExternalSystem.RS)){
				Object skipRsCharging = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_RS_CHARGING);
				if(null == skipRsCharging || (skipRsCharging instanceof Boolean && Boolean.FALSE.equals((Boolean)skipRsCharging))){
					rollbackPerformed = ((FDPRollbackable) fdpCommand).performRollback(fdpRequest);
				}
			}else{
				rollbackPerformed = ((FDPRollbackable) fdpCommand).performRollback(fdpRequest);
			}
		}
		return rollbackPerformed;
	}

	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		boolean executedStep = true;
		FDPLogger.debug(circleLogger, getClass(), "executeStep()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Executing command step for command " + commandDisplayName);
		try {
			if (allowedForMultipleExecution(fdpRequest) || 
					checkcommandtoExecuteMultipleTimes(fdpRequest) || fdpRequest.getExecutedCommand(commandDisplayName) == null) {
				Boolean skipCommand = false;
				updateCommand(fdpRequest);
				if (fdpCommand != null) {
					if(fdpCommand.getSystem().toString().equals(ExternalSystem.RS.toString())){
						Object skipRsCharging = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_RS_CHARGING);
						if(null != skipRsCharging && (skipRsCharging instanceof Boolean && Boolean.TRUE.equals((Boolean)skipRsCharging))){
							skipCommand = true;
							executedStep = true;
							FDPLogger.debug(circleLogger, getClass(), "executeStep()",
									LoggerUtil.getRequestAppender(fdpRequest)
											+ "Skipping command execution");
						}
					}
					if(!skipCommand){
						FDPLogger.debug(circleLogger, getClass(), "executeStep()",
								LoggerUtil.getRequestAppender(fdpRequest)
										+ "Command not executed previously, executing command");
						executedStep = fdpCommand.execute(fdpRequest).equals(Status.SUCCESS);
						fdpRequest.addExecutedCommand(fdpCommand);
					}
				}
			} else {
				fdpCommand = fdpRequest.getExecutedCommand(commandDisplayName);
				executedStep = Status.SUCCESS.equals(fdpCommand.getExecutionStatus());
			}
		} catch (final ExecutionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "executeStep()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "Execution of command failed.", e);
			throw new StepException("Execution of command failed.", e);
		}
		final ResponseError responseError = fdpCommand.getResponseError();
		return (responseError != null) ? RequestUtil.createStepResponse(executedStep, responseError.getResponseCode(),
				responseError.getResponseErrorString(), responseError.getErrorType(), responseError.getSystemType()) : RequestUtil.createStepResponse(executedStep);
	}

	/**
	 * Returns true for commands that can be run multiple time, false otherwise
	 * @param fdpRequest
	 * @return
	 */
	private boolean allowedForMultipleExecution(FDPRequest fdpRequest) {
		return commandDisplayName.contentEquals(Command.DELETEOFFER.getCommandDisplayName());
	}

	// Check to execute again for FAF command. by eagarsh and gur46086
	private boolean checkcommandtoExecuteMultipleTimes(FDPRequest fdpRequest) {
		String fafExecuteCommand = (String)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_EXECUTE_COMMAND_AGAIN);
		if((null != fafExecuteCommand && fafExecuteCommand.equalsIgnoreCase("true")) 
				|| commandDisplayName.contentEquals(Command.UPDATE_BALACEANDATE_MAIN.getCommandDisplayName())
				|| commandDisplayName.contentEquals(Command.UPDATE_ACCUMULATORS.getCommandDisplayName()) || commandDisplayName.contentEquals(Command.GETACCOUNTDETAILS.getCommandDisplayName()) 
				|| commandDisplayName.contentEquals(Command.UPDATE_SERVICE_CLASS.getCommandDisplayName()))
		{
			if((null != fdpRequest.getExecutedCommand(Command.GETACCOUNTDETAILS.getCommandDisplayName())) || null != fdpRequest.getExecutedCommand(Command.UPDATE_FAF.getCommandDisplayName())
					|| null != fdpRequest.getExecutedCommand(Command.UPDATE_BALACEANDATE_MAIN.getCommandDisplayName()) 
					|| null != fdpRequest.getExecutedCommand(Command.UPDATE_ACCUMULATORS.getCommandDisplayName()) || null != fdpRequest.getExecutedCommand(Command.UPDATE_SERVICE_CLASS.getCommandDisplayName())){
				return true;
			}
		}

		return false;
	}

	/**
	 * This method is used to find and update the command for this step.
	 *
	 * @param fdpRequest
	 *            the request.
	 * @throws ExecutionFailedException
	 *             Exception if any.
	 */
	protected void updateCommand(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (fdpCommand == null) {
			final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandDisplayName));
			if (cachedCommand instanceof FDPCommand) {
				fdpCommand = (FDPCommand) cachedCommand;
			}
		}
	}

	@Override
	public String toString() {
		return " command step. Command :- " + commandDisplayName;
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

	public String getCommandDisplayName() {
		return commandDisplayName;
	}

	public FDPCommand getFdpCommand() {
		return fdpCommand;
	}

	public void setFdpCommand(FDPCommand fdpCommand) {
		this.fdpCommand = fdpCommand;
	}

	@Override
	public boolean isStepExecuted() {
		return executed;
	}


	@Override
	public void setStepExecuted(boolean stepexecuted) {
		this.executed=stepexecuted;
	}
	
}