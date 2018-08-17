package com.ericsson.fdp.business.command.rollback.ucip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.command.rollback.helper.CommandRollbackHelper;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This class is used to implement rollback of updateCommunityList command.
 * 
 * @author Ericsson
 */
public class UpdateCommunityListRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1040067829664858500L;
	/** The community id. */
	private final String COMMUNITY_ID = "communityID";
	/** The community information current. */
	private final String COMMUNITY_INFORMATION_CURRENT = "communityInformationCurrent";
	/** The Constant COMMUNITY_INFORMATION_NEW. */
	private final String COMMUNITY_INFORMATION_NEW = "communityInformationNew";
	/** The dot separator. */
	private final String DOT_SEPARATOR = FDPConstant.PARAMETER_SEPARATOR;

	/**
	 * Instantiates a new update community list rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateCommunityListRollback(final String commandDisplayName) {
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
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams)
			throws EvaluationFailedException, ExecutionFailedException {
		boolean communityInformationCurrentFound = false;
		boolean communityInformationNewFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (COMMUNITY_INFORMATION_CURRENT.equals(commandParam.getName())) {
				communityInformationCurrentFound = updateCommunityInformationCurrent(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!communityInformationCurrentFound) {
					commandParamIterator.remove();
				}
			} else if (COMMUNITY_INFORMATION_NEW.equals(commandParam.getName())) {
				communityInformationNewFound = updateCommunityInformationNew(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!communityInformationNewFound) {
					commandParamIterator.remove();
				}
			} else if (commandParam instanceof CommandParamInput) {
				final CommandParamInput input = (CommandParamInput) commandParam;
				input.evaluateValue(fdpRequest);
			}
		}
		if (!communityInformationCurrentFound && !communityInformationNewFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find current and new community Information");
		}
	}

	/**
	 * Update community information current.
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
	private boolean updateCommunityInformationCurrent(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		boolean isPresent = false;
		final List<CommandParam> commandParams = new ArrayList<CommandParam>();
		if (commandParam instanceof CommandParamInput) {
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for community new information
				// element from the update command fired
				final CommandParam commandParamForCommunityInformation = executedUpdateCommand
						.getInputParam(COMMUNITY_INFORMATION_NEW);
				final List<CommandParam> communityInformationChildren = commandParamForCommunityInformation
						.getChilderen();
				// Iterating on children to find the communityID element from
				// the command params
				for (final CommandParam child : communityInformationChildren) {
					final List<CommandParam> children = child.getChilderen();
					for (final CommandParam commandParamObject : children) {
						commandParams.add(commandParamObject);
					}
				}
			}
		}
		// Checking if the new commandParams is empty or not to
		// change the element present flag
		// and transforming the param for rollback command
		if (!commandParams.isEmpty()) {
			transformToRollBackCommand(commandParam, commandParams);
			isPresent = true;
		}
		return isPresent;
	}

	/**
	 * Update community information new.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param object
	 *            the object
	 * @return true, if successful
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private boolean updateCommunityInformationNew(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object object) throws ExecutionFailedException {
		boolean isPresent = false;
		final List<CommandParam> commandParams = new ArrayList<CommandParam>();
		if (commandParam instanceof CommandParamInput) {
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			final CommandParamOutput paramOutput = (CommandParamOutput) commandParamInput.getDefinedValue();
			final FDPCommand fdpGetCommand = fdpRequest.getExecutedCommand(paramOutput.getCommand()
					.getCommandDisplayName());
			// Getting the flatten params string from the command output param
			if (null == fdpGetCommand) {
				throw new ExecutionFailedException("The dependent command "
						+ paramOutput.getCommand().getCommandDisplayName() + " was not executed");
			}
			int i = 0;
			// Iterating till the output param from the get command is not null
			while (fdpGetCommand
					.getOutputParam((COMMUNITY_INFORMATION_CURRENT + DOT_SEPARATOR + i + DOT_SEPARATOR + COMMUNITY_ID)
							.toLowerCase()) != null) {
				final CommandParam communityIDCommandParam = fdpGetCommand
						.getOutputParam((COMMUNITY_INFORMATION_CURRENT + DOT_SEPARATOR + i + DOT_SEPARATOR + COMMUNITY_ID)
								.toLowerCase());
				commandParams.add(communityIDCommandParam);
				i = i + 1;
			}
		}
		// Checking if the new commandParams is empty or not to
		// change the element present flag
		// and transforming the param for rollback command
		if (!commandParams.isEmpty()) {
			transformToRollBackCommand(commandParam, commandParams);
			isPresent = true;
		}
		return isPresent;
	}

	/**
	 * Transform to roll back command.
	 * 
	 * @param commandParamsFetchedFromGet
	 *            the command params fetched from get
	 * @param commandParamsFetchedFromUpdate
	 *            the command params fetched from update
	 */
	private void transformToRollBackCommand(final CommandParam commandParam, final List<CommandParam> paramsList) {
		final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
		final List<CommandParam> usageCounterChildParams = new ArrayList<CommandParam>();
		for (final CommandParam param : paramsList) {
			final List<CommandParam> usageCounterArrayChildren = new ArrayList<CommandParam>();
			// Creating a struct type command param to wrap child objects
			final CommandParamInput commandParamInputStruct = new CommandParamInput(ParameterFeedType.INPUT, null);
			commandParamInputStruct.setType(CommandParameterType.STRUCT);
			commandParamInputStruct.setParent(commandParamInput);

			// Adding an id command param
			usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
					commandParamInputStruct, COMMUNITY_ID, Primitives.INTEGER, param.getValue()));
			commandParamInputStruct.setChilderen(usageCounterArrayChildren);
			usageCounterChildParams.add(commandParamInputStruct);
		}
		commandParamInput.setChilderen(usageCounterChildParams);
	}
}
