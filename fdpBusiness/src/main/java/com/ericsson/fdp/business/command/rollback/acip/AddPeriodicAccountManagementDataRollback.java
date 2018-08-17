package com.ericsson.fdp.business.command.rollback.acip;

import java.util.Iterator;
import java.util.List;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of addPeriodicAccountManagementData
 * command.
 * 
 * @author Ericsson
 */
public class AddPeriodicAccountManagementDataRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8236940196665697877L;
	/** The pam information list. */
	private final String PAM_INFORMATION_LIST = "pamInformationList";

	/**
	 * Instantiates a new adds the periodic account management data rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public AddPeriodicAccountManagementDataRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		try {
			// extracting command parameters from two source commands
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
	 * @throws EvaluationFailedException
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams)
			throws ExecutionFailedException, EvaluationFailedException {
		boolean pamInformationListFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (commandParam instanceof CommandParamInput) {
				final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
				if (PAM_INFORMATION_LIST.equals(commandParamInput.getName())) {
					pamInformationListFound = updatePamInformationList(commandParamInput, fdpRequest,
							otherParams != null ? otherParams[0] : null);
					if (!pamInformationListFound) {
						commandParamIterator.remove();
					}
				} else {
					commandParamInput.evaluateValue(fdpRequest);
				}
			}
		}
		if (!pamInformationListFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find pam Information List");
		}
	}

	/**
	 * Update pam information list.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParam
	 *            the other param
	 * @return true, if successful
	 */
	private boolean updatePamInformationList(final CommandParamInput commandParamInput, final FDPRequest fdpRequest,
			final Object otherParam) {
    		boolean isPresent = false;
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for pam information list
				// element from the update command fired
				final CommandParam pamInfoListParam = executedUpdateCommand.getInputParam(PAM_INFORMATION_LIST);
				if (pamInfoListParam != null) {
					final List<CommandParam> pamInfoChildren = pamInfoListParam.getChilderen();
					commandParamInput.setChilderen(pamInfoChildren);
					isPresent = true;
				}
			}
		return isPresent;
	}
}
