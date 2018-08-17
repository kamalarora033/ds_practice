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
 * This class is used to implement rollback of updatePromotionCounters command.
 * 
 * @author Ericsson
 */
public class UpdatePromotionCountersRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -8754739706373845199L;
	/** The progression refill counter step relative. */
	private final String PROGRESSION_REFILL_COUNTER_STEP_RELATIVE = "progressionRefillCounterStepRelative";
	/** The progression refill amount relative. */
	private final String PROGRESSION_REFILL_AMOUNT_RELATIVE = "progressionRefillAmountRelative";
	/** The promotion refill counter step relative. */
	private final String PROMOTION_REFILL_COUNTER_STEP_RELATIVE = "promotionRefillCounterStepRelative";
	/** The promotion refill amount relative. */
	private final String PROMOTION_REFILL_AMOUNT_RELATIVE = "promotionRefillAmountRelative";
	/** The transaction currency. */
	private final String TRANSACTION_CURRENCY = "transactionCurrency";
	/** The origin operator id. */
	private final String ORIGIN_OPERATOR_ID = "originOperatorID";
	/** The Constant MINUS. */
	private final String MINUS = "-";

	/**
	 * Instantiates a new update promotion counters rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdatePromotionCountersRollback(final String commandDisplayName) {
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
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams) {
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (ORIGIN_OPERATOR_ID.equals(commandParam)) {
				updateCommandParam(commandParam, fdpRequest, otherParams != null ? otherParams[0] : null,
						ORIGIN_OPERATOR_ID, false);
			} else if (TRANSACTION_CURRENCY.equals(commandParam)) {
				updateCommandParam(commandParam, fdpRequest, otherParams != null ? otherParams[0] : null,
						TRANSACTION_CURRENCY, false);
			} else if (PROMOTION_REFILL_AMOUNT_RELATIVE.equals(commandParam)) {
				updateCommandParam(commandParam, fdpRequest, otherParams != null ? otherParams[0] : null,
						PROMOTION_REFILL_AMOUNT_RELATIVE, true);
			} else if (PROMOTION_REFILL_COUNTER_STEP_RELATIVE.equals(commandParam)) {
				updateCommandParam(commandParam, fdpRequest, otherParams != null ? otherParams[0] : null,
						PROMOTION_REFILL_COUNTER_STEP_RELATIVE, true);
			} else if (PROGRESSION_REFILL_AMOUNT_RELATIVE.equals(commandParam)) {
				updateCommandParam(commandParam, fdpRequest, otherParams != null ? otherParams[0] : null,
						PROGRESSION_REFILL_AMOUNT_RELATIVE, true);
			} else if (PROGRESSION_REFILL_COUNTER_STEP_RELATIVE.equals(commandParam)) {
				updateCommandParam(commandParam, fdpRequest, otherParams != null ? otherParams[0] : null,
						PROGRESSION_REFILL_COUNTER_STEP_RELATIVE, true);
			}
		}

	}

	/**
	 * Update command param.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParam
	 *            the other param
	 * @param paramName
	 *            the param name
	 * @param isInvertible
	 *            the is invertible
	 */
	private void updateCommandParam(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam, final String paramName, final boolean isInvertible) {
		if (commandParam instanceof CommandParamInput) {
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				final CommandParam commandParamObj = executedUpdateCommand.getInputParam(paramName);
				if (null == commandParamObj) {
					return;
				}
				final Object commandParamValue = commandParamObj.getValue();
				Object valueToSet = null;
				if (isInvertible) {
					if (commandParamValue instanceof String) {
						final String value = (String) commandParamValue;
						final StringBuilder sb = new StringBuilder(value);
						sb.insert(0, MINUS);
						valueToSet = sb.toString();
					} else if (commandParamValue instanceof Integer) {
						final Integer value = (Integer) commandParamValue;
						valueToSet = value * -1;
					}
				} else {
					valueToSet = commandParamValue;
				}
				commandParamInput.setValue(valueToSet);
			}
		}
	}
}
