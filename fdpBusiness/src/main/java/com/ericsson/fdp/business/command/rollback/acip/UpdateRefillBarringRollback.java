package com.ericsson.fdp.business.command.rollback.acip;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of updateRefillBarring command.
 * 
 * @author Ericsson
 */
public class UpdateRefillBarringRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2043982401099906011L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRefillBarringRollback.class);

	/** The refill bar action. */
	private final String REFILL_BAR_ACTION = "refillBarAction";
	/** The refill unbar date time. */
	private final String REFILL_UNBAR_DATE_TIME = "refillUnbarDateTime";
	/** The refill bar action type bar. */
	private final String REFILL_BAR_ACTION_TYPE_BAR = "BAR";

	/**
	 * Instantiates a new update refill barring rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateRefillBarringRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		try {
			// extracting command parameters from source commands
			extractionFromSourceCommands(fdpRequest, otherParams);
			return executeCommand(fdpRequest);
		} catch (final EvaluationFailedException e) {
			throw new ExecutionFailedException("Could not execute command", e);
		}
	}

	/**
	 * Extraction from source commands.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParams
	 *            the other params
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams)
			throws ExecutionFailedException {
		boolean isRollbackRequired = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (REFILL_UNBAR_DATE_TIME.equals(commandParam.getName())) {
				isRollbackRequired = updateRefillBarringInfo(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!isRollbackRequired) {
					commandParamIterator.remove();
				}
			}
		}
		if (!isRollbackRequired) {
			LOGGER.debug("No Rollback Required for this command, the action was not equal to 'BAR'");
		}
	}

	/**
	 * Update refill barring info.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParam
	 *            the other param
	 * @return true, if successful
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private boolean updateRefillBarringInfo(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		boolean isRollbackRequired = false;
		if (commandParam instanceof CommandParamInput) {
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			final CommandParamOutput paramOutput = (CommandParamOutput) commandParamInput.getDefinedValue();
			final FDPCommand fdpGetCommand = fdpRequest.getExecutedCommand(paramOutput.getCommand()
					.getCommandDisplayName());
			// Getting the flatten params string from the command output param
			// final String flattenParam = paramOutput.flattenParam();
			if (null == fdpGetCommand) {
				throw new ExecutionFailedException("The dependent command "
						+ paramOutput.getCommand().getCommandDisplayName() + " was not executed");
			}
			final CommandParam refillUnbarDateTimeParam = fdpGetCommand.getOutputParam(REFILL_UNBAR_DATE_TIME
					.toLowerCase());
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for counter usage information
				// element from the update command fired
				final CommandParam commandParamForRefillBarAction = executedUpdateCommand
						.getInputParam(REFILL_BAR_ACTION);
				if (commandParamForRefillBarAction.getValue().equals(REFILL_BAR_ACTION_TYPE_BAR)) {
					commandParamInput.setValue(refillUnbarDateTimeParam.getValue());
					isRollbackRequired = true;
				}
			} else {
				throw new ExecutionFailedException("");
			}
		}
		return isRollbackRequired;
	}

}
