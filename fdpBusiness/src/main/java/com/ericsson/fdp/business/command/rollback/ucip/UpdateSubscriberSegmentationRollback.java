package com.ericsson.fdp.business.command.rollback.ucip;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.command.rollback.helper.CommandRollbackHelper;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of updateSubscriberSegmentation
 * command.
 * 
 * @author Ericsson
 */
public class UpdateSubscriberSegmentationRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -7023295441825345084L;

	/** The service offerings information. */
	private final String SERVICE_OFFERINGS_INFORMATION = "serviceOfferings";

	/** The service offering id. */
	private final String SERVICE_OFFERING_ID = "serviceOfferingID";

	/** The service offering active flag. */
	private final String SERVICE_OFFERING_ACTIVE_FLAG = "serviceOfferingActiveFlag";

	/** The dot separator. */
	private final String DOT_SEPARATOR = FDPConstant.PARAMETER_SEPARATOR;

	/**
	 * Instantiates a new update subscriber segmentation rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateSubscriberSegmentationRollback(final String commandDisplayName) {
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

	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams)
			throws EvaluationFailedException, ExecutionFailedException {
		boolean serviceOfferingsFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (SERVICE_OFFERINGS_INFORMATION.equals(commandParam.getName())) {
				serviceOfferingsFound = updateServiceOfferingsInformation(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!serviceOfferingsFound) {
					commandParamIterator.remove();
				}
			} else if (commandParam instanceof CommandParamInput) {
				final CommandParamInput input = (CommandParamInput) commandParam;
				input.evaluateValue(fdpRequest);
			}
		}
		if (!serviceOfferingsFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find service Offerings information");
		}
	}

	private boolean updateServiceOfferingsInformation(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		boolean isPresent = false;
		final Map<Object, CommandParam> updatedParamMapForTransaction = new LinkedHashMap<Object, CommandParam>();
		if (commandParam instanceof CommandParamInput) {
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			final CommandParamOutput paramOutput = (CommandParamOutput) commandParamInput.getDefinedValue();
			final FDPCommand fdpGetCommand = fdpRequest.getExecutedCommand(paramOutput.getCommand()
					.getCommandDisplayName());
			// Getting the flatten params string from the command output param
			final String flattenParam = paramOutput.flattenParam();
			if (null == fdpGetCommand) {
				throw new ExecutionFailedException("The dependent command "
						+ paramOutput.getCommand().getCommandDisplayName() + " was not executed");
			}
			// Creating a map to maintain the key-value pair of parameterId and
			// commandparam object
			final Map<Object, CommandParam> commandParameterMap = new LinkedHashMap<Object, CommandParam>();
			int i = 0;
			// Iterating till the output param from the get command is not null
			while (fdpGetCommand
					.getOutputParam((flattenParam + DOT_SEPARATOR + i + DOT_SEPARATOR + SERVICE_OFFERING_ID)
							.toLowerCase()) != null) {
				final CommandParam serviceOfferingIDParam = fdpGetCommand.getOutputParam((flattenParam + DOT_SEPARATOR
						+ i + DOT_SEPARATOR + SERVICE_OFFERING_ID).toLowerCase());
				final CommandParam serviceOfferringActiveFlagParam = fdpGetCommand.getOutputParam((flattenParam
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + SERVICE_OFFERING_ACTIVE_FLAG).toLowerCase());
				commandParameterMap.put(serviceOfferingIDParam.getValue(), serviceOfferringActiveFlagParam);
				i = i + 1;
			}
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for service offerings information
				// element from the update command fired
				final CommandParam commandParamForServiceOfferings = executedUpdateCommand
						.getInputParam(SERVICE_OFFERINGS_INFORMATION);
				// serviceOfferings element was not found in update command, as
				// it is not mandatory.
				if (null == commandParamForServiceOfferings) {
					return isPresent;
				}
				final List<CommandParam> serviceOfferingChildren = commandParamForServiceOfferings.getChilderen();
				// Iterating on children to find the serviceOfferingID element
				// from
				// the command params
				for (final CommandParam child : serviceOfferingChildren) {
					final List<CommandParam> commandParams = child.getChilderen();
					for (final CommandParam commandParamObject : commandParams) {
						// Checking if the serviceOfferingID is found while
						// traversing the command params.
						if (SERVICE_OFFERING_ID.equals(commandParamObject.getName())) {
							final Object value = commandParamObject.getValue();
							final CommandParam commandParamValueObj = commandParameterMap.get(value);
							// Checking if serviceOfferingID value is present in
							// the map container holding the params from Get
							// command.
							if (null == commandParamValueObj) {
								final CommandParamInput commandParamForActiveFlag = CommandRollbackHelper
										.getCommandInputObjectForPrimitive(null, SERVICE_OFFERING_ACTIVE_FLAG,
												Primitives.BOOLEAN, 0);
								updatedParamMapForTransaction.put(value, commandParamForActiveFlag);
							} else {
								updatedParamMapForTransaction.put(value, commandParamValueObj);
							}
						}
					}
				}
			} else {
				throw new ExecutionFailedException("");
			}
		}
		// Checking if the new updatedParamMapForTransaction is empty or not to
		// change the element present flag
		// and transforming the param for rollback command
		if (!updatedParamMapForTransaction.isEmpty()) {
			CommandRollbackHelper.transformationToRollBack(commandParam, updatedParamMapForTransaction,
					SERVICE_OFFERING_ID, SERVICE_OFFERING_ACTIVE_FLAG);
			isPresent = true;
		}
		return isPresent;
	}
}