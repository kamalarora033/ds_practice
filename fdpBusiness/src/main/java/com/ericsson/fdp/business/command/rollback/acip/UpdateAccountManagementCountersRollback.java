package com.ericsson.fdp.business.command.rollback.acip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public class UpdateAccountManagementCountersRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 4375341534403641038L;
	/** The counter update information. */
	private final String COUNTER_UPDATE_INFORMATION = "counterUpdateInformation";
	/** The counter clearing date. */
	private final String COUNTER_CLEARING_DATE = "counterClearingDate";
	/** The period counter relative value. */
	private final String PERIOD_COUNTER_RELATIVE_VALUE = "periodCounterRelativeValue";
	/** The total counter relative value. */
	private final String TOTAL_COUNTER_RELATIVE_VALUE = "totalCounterRelativeValue";

	/**
	 * Instantiates a new update account management counters rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateAccountManagementCountersRollback(final String commandDisplayName) {
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
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams)
			throws ExecutionFailedException {
		boolean counterUpdateInformationFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (COUNTER_UPDATE_INFORMATION.equals(commandParam.getName())) {
				counterUpdateInformationFound = updateCounterUpdateInformation(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!counterUpdateInformationFound) {
					commandParamIterator.remove();
				}
			}
		}
		if (!counterUpdateInformationFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find Counter Update Information");
		}
	}

	/**
	 * Update counter update information.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParam
	 *            the other param
	 * @return true, if successful
	 */
	private boolean updateCounterUpdateInformation(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) {
		boolean isPresent = false;
		if (commandParam instanceof CommandParamInput) {
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for counter update information
				// element from the update command fired
				final CommandParam counterUpdateInfoParam = executedUpdateCommand
						.getInputParam(COUNTER_UPDATE_INFORMATION);
				final List<CommandParam> counterUpdateInfoChildren = counterUpdateInfoParam.getChilderen();
				for (final CommandParam child : counterUpdateInfoChildren) {
					final List<CommandParam> commandParams = child.getChilderen();
					for (final CommandParam commandParamObject : commandParams) {
						final CommandParamInput paramInput = (CommandParamInput) commandParamObject;
						final String paramName = commandParamObject.getName();
						final Object paramValue = paramInput.getValue();
						if (TOTAL_COUNTER_RELATIVE_VALUE.equals(paramName)) {
							final Integer valueToSet = (Integer) paramValue;
							paramInput.setValue(valueToSet * -1);
						} else if (PERIOD_COUNTER_RELATIVE_VALUE.equals(paramName)) {
							final Integer valueToSet = (Integer) paramValue;
							paramInput.setValue(valueToSet * -1);
						} else if (COUNTER_CLEARING_DATE.equals(paramName)) {
							final SimpleDateFormat df = new SimpleDateFormat(FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
							paramInput.setValue(df.format(Calendar.getInstance().getTime()));
						}
					}
				}
				commandParamInput.setChilderen(counterUpdateInfoChildren);
				isPresent = true;
			}
		}
		return isPresent;
	}
}
