package com.ericsson.fdp.business.command.rollback.acip;

import java.util.Iterator;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of updateTemporaryBlocked command.
 * 
 * @author Ericsson
 */
public class UpdateTemporaryBlockedRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -4534689646534158406L;

	/** The temporary blocked flag. */
	private final String TEMPORARY_BLOCKED_FLAG = "temporaryBlockedFlag";

	public UpdateTemporaryBlockedRollback(final String commandDisplayName) {
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
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams)
			throws ExecutionFailedException {
		boolean temporaryBlockedFlagFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (TEMPORARY_BLOCKED_FLAG.equals(commandParam.getName())) {
				temporaryBlockedFlagFound = updateTemporaryBlockedFlag(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!temporaryBlockedFlagFound) {
					commandParamIterator.remove();
				}
			}
		}
		if (!temporaryBlockedFlagFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find temporary Blocked Flag");
		}

	}

	/**
	 * Update temporary blocked flag.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param object
	 *            the object
	 * @return true, if successful
	 * @throws ExecutionFailedException
	 */
	private boolean updateTemporaryBlockedFlag(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		boolean isPresent = false;
		if (otherParam instanceof FDPCommand) {
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
			// Getting the command param for counter usage information
			// element from the update command fired
			final CommandParam temporaryBlockedFlagParam = executedUpdateCommand.getInputParam(TEMPORARY_BLOCKED_FLAG);
			if (null == temporaryBlockedFlagParam) {
				return isPresent;
			}
			final Boolean paramValue = (Boolean) temporaryBlockedFlagParam.getValue();
			if (paramValue.equals(0)) {
				commandParamInput.setValue(1);
			} else if (paramValue.equals(1)) {
				commandParamInput.setValue(0);
			}
			isPresent = true;
		} else {
			throw new ExecutionFailedException("");
		}
		return isPresent;
	}
}
