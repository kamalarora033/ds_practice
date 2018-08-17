package com.ericsson.fdp.business.cache;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.Function;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.dao.enums.CommandDefinition;
import com.ericsson.fdp.dao.enums.CommandExecutionType;
import com.ericsson.fdp.dao.enums.CommandParameterType;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * The Class BatchJobCommandsForCache.
 *
 * @author Ericsson
 */
public final class BatchJobCommandsForCache {

	/**
	 * Instantiates a new batch job commands for cache.
	 */
	private BatchJobCommandsForCache() {
	}

	/**
	 * Load default batch job commands.
	 *
	 * @return the list
	 */
	public static List<FDPCommand> loadDefaultBatchJobCommands() {
		final List<FDPCommand> result = new ArrayList<FDPCommand>();
		NonTransactionCommand command =
				new NonTransactionCommand(Command.DELETE_OFFER_FOR_BATCH_JOB.getCommandDisplayName());
		command.setCommandExecutionType(CommandExecutionType.ACIP);
		command.setCommandType(CommandDefinition.DELETE);
		command.setSystem(ExternalSystem.AIR);
		command.setInputParam(getInputParamForCommand(command));
		command.setCommandName(Command.DELETE_OFFER_FOR_BATCH_JOB.getCommandName());
		result.add(command);
		command =
				new NonTransactionCommand(
						Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB.getCommandDisplayName());
		command.setCommandExecutionType(CommandExecutionType.UCIP);
		command.setCommandType(CommandDefinition.GET);
		command.setSystem(ExternalSystem.AIR);
		command.setInputParam(getInputParamForCommand(command));
		command.setCommandName(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB.getCommandName());
		result.add(command);
		command =
				new NonTransactionCommand(
						Command.UPDATE_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB.getCommandDisplayName());
		command.setCommandExecutionType(CommandExecutionType.UCIP);
		command.setCommandType(CommandDefinition.UPDATE);
		command.setSystem(ExternalSystem.AIR);
		command.setInputParam(getInputParamForCommand(command));
		command.setCommandName(Command.UPDATE_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB.getCommandName());
		result.add(command);
		command =
				new NonTransactionCommand(
						Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_CONSUMER.getCommandDisplayName());
		command.setCommandExecutionType(CommandExecutionType.UCIP);
		command.setCommandType(CommandDefinition.GET);
		command.setSystem(ExternalSystem.AIR);
		command.setInputParam(getInputParamForCommand(command));
		command.setCommandName(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_CONSUMER.getCommandName());
		result.add(command);

		command = new NonTransactionCommand(Command.GET_OFFER_FOR_SHARED_ACC.getCommandDisplayName());
		command.setCommandExecutionType(CommandExecutionType.UCIP);
		command.setCommandType(CommandDefinition.GET);
		command.setSystem(ExternalSystem.AIR);
		command.setInputParam(getInputParamForCommand(command));
		command.setCommandName(Command.GET_OFFER_FOR_SHARED_ACC.getCommandName());
		result.add(command);

		return result;
	}

	/**
	 * Gets the input param for command.
	 *
	 * @param command
	 *            the command
	 * @return the input param for command
	 */
	private static List<CommandParam> getInputParamForCommand(final FDPCommand command) {
		final List<CommandParam> result = new ArrayList<CommandParam>();
		if (command.getCommandDisplayName().equals(Command.DELETE_OFFER_FOR_BATCH_JOB.getCommandDisplayName())) {
			result.add(getParameter("originNodeType", "originNodeType", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originHostName", "originHostName", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTransactionID", "originTransactionID", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTimeStamp", Function.NOW, Primitives.DATETIME, command,
					ParameterFeedType.FUNCTION));
			result.add(getParameter("subscriberNumber", "subscriberNumber", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("offerID", AuxRequestParam.OFFER_ID, Primitives.INTEGER, command,
					ParameterFeedType.AUX_REQUEST_PARAM));
		} else if (command.getCommandDisplayName().equals(
				Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB.getCommandDisplayName())) {
			result.add(getParameter("originNodeType", "originNodeType", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originHostName", "originHostName", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTransactionID", "originTransactionID", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTimeStamp", Function.NOW, Primitives.DATETIME, command,
					ParameterFeedType.FUNCTION));
			result.add(getParameter("subscriberNumber", "subscriberNumber", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
		} else if (command.getCommandDisplayName().equals(
				Command.UPDATE_USAGE_THRESHOLDS_AND_COUNTERS_FOR_BATCH_JOB.getCommandDisplayName())) {
			result.add(getParameter("originNodeType", "originNodeType", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originHostName", "originHostName", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTransactionID", "originTransactionID", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTimeStamp", Function.NOW, Primitives.DATETIME, command,
					ParameterFeedType.FUNCTION));
			result.add(getParameter("subscriberNumber", "subscriberNumber", Primitives.STRING, command,
					ParameterFeedType.REQUEST));

			final CommandParamInput commandParamInput =
					(CommandParamInput) getParameter("usageCounterUpdateInformation", null, null, command,
							ParameterFeedType.AUX_REQUEST_PARAM);
			commandParamInput.setType(CommandParameterType.STRUCT);
			final List<CommandParam> childs = new ArrayList<CommandParam>();
			commandParamInput.setChilderen(childs);
			result.add(commandParamInput);

			childs.add(getParameter("usageCounterID", AuxRequestParam.VALID_ID, Primitives.LONG, command,
					ParameterFeedType.AUX_REQUEST_PARAM));
			childs.add(getParameter("usageCounterValueNew", AuxRequestParam.CONSUMER_LIMIT_UPGRADE_VALUE,
					Primitives.INTEGER, command, ParameterFeedType.AUX_REQUEST_PARAM));
			result.add(getParameter("associatedPartyID.", AuxRequestParam.CONSUMER_MSISDN, Primitives.LONG, command,
					ParameterFeedType.AUX_REQUEST_PARAM));
		} else if (command.getCommandDisplayName().equals(
				Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_FOR_CONSUMER.getCommandDisplayName())) {

			result.add(getParameter("originNodeType", "originNodeType", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originHostName", "originHostName", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTransactionID", "originTransactionID", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTimeStamp", Function.NOW, Primitives.DATETIME, command,
					ParameterFeedType.FUNCTION));
			result.add(getParameter("subscriberNumber", "subscriberNumber", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("associatedPartyID", AuxRequestParam.CONSUMER_MSISDN, Primitives.STRING, command,
					ParameterFeedType.AUX_REQUEST_PARAM));
		} else if (command.getCommandDisplayName().equals(Command.GET_OFFER_FOR_SHARED_ACC.getCommandDisplayName())) {

			result.add(getParameter("originNodeType", "originNodeType", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originHostName", "originHostName", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTransactionID", "originTransactionID", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("originTimeStamp", Function.NOW, Primitives.DATETIME, command,
					ParameterFeedType.FUNCTION));
			result.add(getParameter("subscriberNumber", "subscriberNumber", Primitives.STRING, command,
					ParameterFeedType.REQUEST));
			result.add(getParameter("subscriberNumberNAI", 2, Primitives.INTEGER, command, ParameterFeedType.INPUT));
			result.add(getParameter("offerRequestedTypeFlag", "11111000", Primitives.STRING, command,
					ParameterFeedType.INPUT));
			result.add(getParameter("requestInactiveOffersFlag", 1, Primitives.BOOLEAN, command,
					ParameterFeedType.INPUT));
			result.add(getParameter("requestSubDedicatedAccountDetailsFlag", 1, Primitives.BOOLEAN, command,
					ParameterFeedType.INPUT));
			result.add(getParameter("requestAggregatedProductOfferInformationFlag", 1, Primitives.BOOLEAN, command,
					ParameterFeedType.INPUT));
			result.add(getParameter("requestDedicatedAccountDetailsFlag", 1, Primitives.BOOLEAN, command,
					ParameterFeedType.INPUT));

		}
		return result;
	}

	/**
	 * Gets the parameter.
	 *
	 * @param name
	 *            the name
	 * @param value
	 *            the value
	 * @param type
	 *            the type
	 * @param command
	 *            the command
	 * @param feedType
	 * @return the parameter
	 */
	private static CommandParam getParameter(final String name, final Object value, final Primitives type,
			final FDPCommand command, final ParameterFeedType feedType) {
		final CommandParamInput param = new CommandParamInput(feedType, value);
		param.setType(CommandParameterType.PRIMITIVE);
		param.setPrimitiveValue(type);
		param.setCommand(command);
		param.setName(name);
		return param;
	}

}
