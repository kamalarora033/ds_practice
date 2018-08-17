package com.ericsson.fdp.business.command.rollback.acip;

import java.util.Iterator;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This class is used to implement rollback of updatePromotionPlan command.
 * 
 * @author Ericsson
 */
public class UpdatePromotionPlanRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -55435921525187315L;
	/** The promotion plan action. */
	private final String PROMOTION_PLAN_ACTION = "promotionPlanAction";
	/** The promotion plan action set. */
	private final String PROMOTION_PLAN_ACTION_SET = "SET";
	/** The promotion plan action delete. */
	private final String PROMOTION_PLAN_ACTION_DELETE = "DELETE";
	/** The promotion plan action add. */
	private final String PROMOTION_PLAN_ACTION_ADD = "ADD";
	/** The promotion end date. */
	private final String PROMOTION_END_DATE = "promotionEndDate";
	/** The promotion start date. */
	private final String PROMOTION_START_DATE = "promotionStartDate";
	/** The promotion plan id. */
	private final String PROMOTION_PLAN_ID = "promotionPlanID";

	/**
	 * Instantiates a new update promotion plan rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdatePromotionPlanRollback(final String commandDisplayName) {
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
		boolean promotionPlanActionFound = false;
		boolean promotionPlanIDFound = false;
		boolean promotionStartDateFound = false;
		boolean promotionEndDateFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			final String parameterName = commandParam.getName();
			if (PROMOTION_PLAN_ACTION.equals(parameterName)) {
				promotionPlanActionFound = updatePromotionPlanAction(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!promotionPlanActionFound) {
					commandParamIterator.remove();
				}
			} else if (PROMOTION_PLAN_ID.equals(parameterName)) {
				promotionPlanIDFound = populateCommandParamForRollback(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null, PROMOTION_PLAN_ID);
				if (!promotionPlanIDFound) {
					commandParamIterator.remove();
				}
			} else if (PROMOTION_START_DATE.equals(parameterName)) {
				promotionStartDateFound = populateCommandParamForRollback(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null, PROMOTION_START_DATE);
				if (!promotionStartDateFound) {
					commandParamIterator.remove();
				}
			} else if (PROMOTION_END_DATE.equals(parameterName)) {
				promotionEndDateFound = populateCommandParamForRollback(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null, PROMOTION_END_DATE);
				if (!promotionEndDateFound) {
					commandParamIterator.remove();
				}
			}
		}
		if (!promotionPlanActionFound && !promotionPlanIDFound && !promotionStartDateFound && promotionEndDateFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find mandatory parameters to execute the rollback command");
		}
	}

	/**
	 * Update promotion plan.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param object
	 *            the object
	 * @return true, if successful
	 */
	private boolean updatePromotionPlanAction(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) {
		boolean isPresent = false;
		if (commandParam instanceof CommandParamInput) {
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				final CommandParam promotionPlanActionParam = executedUpdateCommand
						.getInputParam(PROMOTION_PLAN_ACTION);
				final String promotionPlanAction = (String) promotionPlanActionParam.getValue();
				final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
				commandParamInput.setType(CommandParameterType.PRIMITIVE);
				commandParamInput.setPrimitiveValue(Primitives.STRING);
				if (PROMOTION_PLAN_ACTION_ADD.equals(promotionPlanAction)) {
					commandParamInput.setValue(PROMOTION_PLAN_ACTION_DELETE);
				} else if (PROMOTION_PLAN_ACTION_DELETE.equals(promotionPlanAction)) {
					commandParamInput.setValue(PROMOTION_PLAN_ACTION_ADD);
				} else {
					commandParamInput.setValue(PROMOTION_PLAN_ACTION_SET);
				}
				isPresent = true;
			}
		}

		return isPresent;
	}

	/**
	 * Populate command param for rollback.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParam
	 *            the other param
	 * @param paramToPopulate
	 *            the param to populate
	 * @return true, if successful
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private boolean populateCommandParamForRollback(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam, final String paramToPopulate) throws ExecutionFailedException {
		boolean isPresent = false;
		final CommandParam promotionPlanActionParam = this.getInputParam(PROMOTION_PLAN_ACTION);
		if (commandParam instanceof CommandParamInput) {
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			final CommandParamOutput paramOutput = (CommandParamOutput) commandParamInput.getDefinedValue();
			if (promotionPlanActionParam.equals(PROMOTION_PLAN_ACTION_SET)) {
				final FDPCommand fdpGetCommand = fdpRequest.getExecutedCommand(paramOutput.getCommand()
						.getCommandDisplayName());
				if (null == fdpGetCommand) {
					throw new ExecutionFailedException("The dependent command "
							+ paramOutput.getCommand().getCommandDisplayName() + " was not executed");
				}
				final CommandParam param = fdpGetCommand.getOutputParam(paramToPopulate.toLowerCase());
				if (null == param) {
					return isPresent;
				}
				commandParamInput.setValue(param.getValue());
				isPresent = true;
			} else {
				if (otherParam instanceof FDPCommand) {
					final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
					final CommandParam param = executedUpdateCommand.getInputParam(paramToPopulate);
					commandParamInput.setValue(param.getValue());
				} else {
					throw new ExecutionFailedException("");
				}
			}

		}
		return isPresent;
	}
}
