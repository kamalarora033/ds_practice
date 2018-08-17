package com.ericsson.fdp.business.command.rollback.ucip;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.command.rollback.helper.CommandRollbackHelper;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This class is used to implement rollback of updateBalanceAndDate command.
 * 
 * @author Ericsson
 */
public class UpdateBalanceAndDateRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -261813164131417601L;

	/** The dedicated account update information. */
	private final String DEDICATED_ACCOUNT_INFORMATION = "dedicatedAccountInformation";
	/** The dedicated account update information. */
	private final String DEDICATED_ACCOUNT_UPDATE_INFORMATION = "dedicatedAccountUpdateInformation";
	/** The dedicated account id. */
	private final String DEDICATED_ACCOUNT_ID = "dedicatedAccountID";
	/** The expiry date. */
	private final String EXPIRY_DATE = "expiryDate";
	/** The start date. */
	private final String START_DATE = "startDate";
	/** The update action. */
	private final String UPDATE_ACTION = "updateAction";
	/** The dedicated account unit type. */
	private final String DEDICATED_ACCOUNT_UNIT_TYPE = "dedicatedAccountUnitType";
	/** The update action expire. */
	private final String UPDATE_ACTION_EXPIRE = "EXPIRE";
	/** The dot separator. */
	private final String DOT_SEPARATOR = FDPConstant.PARAMETER_SEPARATOR;

	/**
	 * Instantiates a new update balance and date rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateBalanceAndDateRollback(final String commandDisplayName) {
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
		boolean dedicatedAccountInformationFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (DEDICATED_ACCOUNT_UPDATE_INFORMATION.equals(commandParam.getName())) {
				dedicatedAccountInformationFound = updateDedicatedAccountInformation(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!dedicatedAccountInformationFound) {
					commandParamIterator.remove();
				}
			} else if (commandParam instanceof CommandParamInput) {
				try {
					((CommandParamInput) commandParam).evaluateValue(fdpRequest);
				} catch (final EvaluationFailedException e) {
					throw new ExecutionFailedException("Could not evaluate value ", e);
				}
			}
		}
		// if (!dedicatedAccountInformationFound) {
		// // Could not find any of the required parameter.
		// throw new
		// ExecutionFailedException("Could not find dedicated Account Information");
		// }

	}

	/**
	 * Update dedicated account information.
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
	private boolean updateDedicatedAccountInformation(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		boolean isPresent = false;
		final Map<Object, DedicatedAccountInfoVO> updatedParamMapForTransaction = new LinkedHashMap<Object, DedicatedAccountInfoVO>();
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
			// Creating a map to maintain the key-value pair of parameterId and
			// commandparam object
			final Map<Object, DedicatedAccountInfoVO> commandParameterMap = new LinkedHashMap<Object, DedicatedAccountInfoVO>();
			int i = 0;
			// Iterating till the output param from the get command is not null
			while (fdpGetCommand
					.getOutputParam((DEDICATED_ACCOUNT_INFORMATION + DOT_SEPARATOR + i + DOT_SEPARATOR + DEDICATED_ACCOUNT_ID)
							.toLowerCase()) != null) {
				final CommandParam dedicatedAccountID = fdpGetCommand.getOutputParam((DEDICATED_ACCOUNT_INFORMATION
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + DEDICATED_ACCOUNT_ID).toLowerCase());
				final CommandParam dedicatedAccountUnitType = fdpGetCommand
						.getOutputParam((DEDICATED_ACCOUNT_INFORMATION + DOT_SEPARATOR + i + DOT_SEPARATOR + DEDICATED_ACCOUNT_UNIT_TYPE)
								.toLowerCase());
				final CommandParam startDate = fdpGetCommand.getOutputParam((DEDICATED_ACCOUNT_INFORMATION
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + START_DATE).toLowerCase());
				final CommandParam expiryDate = fdpGetCommand.getOutputParam((DEDICATED_ACCOUNT_INFORMATION
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + EXPIRY_DATE).toLowerCase());
				final DedicatedAccountInfoVO dedicatedAccountInfo = new DedicatedAccountInfoVO(dedicatedAccountID,
						dedicatedAccountUnitType, startDate, expiryDate);
				commandParameterMap.put(dedicatedAccountID.getValue(), dedicatedAccountInfo);
				i = i + 1;
			}
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for dedicated account information
				// element from the update command fired
				final CommandParam commandParamForDedicatedUpdateInfo = executedUpdateCommand
						.getInputParam(DEDICATED_ACCOUNT_UPDATE_INFORMATION);
				// dedicatedAccountUpdateInformation element was not found in
				// update command, as it is not mandatory.
				if (null != commandParamForDedicatedUpdateInfo) {
					final List<CommandParam> dedicatedInfoChildren = commandParamForDedicatedUpdateInfo.getChilderen();
					for (final CommandParam child : dedicatedInfoChildren) {
						final List<CommandParam> commandParams = child.getChilderen();
						for (final CommandParam commandParamObject : commandParams) {
							// Checking if the dedicatedAccountID is found while
							// traversing the command params.
							if (DEDICATED_ACCOUNT_ID.equals(commandParamObject.getName())) {
								final Object commandParamValue = commandParamObject.getValue();
								DedicatedAccountInfoVO accountInfoVO = commandParameterMap.get(commandParamValue);
								if (null == accountInfoVO) {
									final CommandParam updateAction = CommandRollbackHelper
											.getCommandInputObjectForPrimitive(null, UPDATE_ACTION, Primitives.STRING,
													UPDATE_ACTION_EXPIRE);
									accountInfoVO = new DedicatedAccountInfoVO(commandParamObject, updateAction);
									updatedParamMapForTransaction.put(commandParamValue, accountInfoVO);
								} else {
									updatedParamMapForTransaction.put(commandParamValue, accountInfoVO);
								}
							}
							break;
						}
					}
				} else {
					for (final Map.Entry<Object, UpdateBalanceAndDateRollback.DedicatedAccountInfoVO> entrySet : commandParameterMap
							.entrySet()) {
						updatedParamMapForTransaction.put(entrySet.getValue(), entrySet.getValue());
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
			transformToRollbackCommand(commandParam, updatedParamMapForTransaction);
			isPresent = true;
		}
		return isPresent;
	}

	/**
	 * Transform to rollback command.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param updatedParamMapForTransaction
	 *            the updated param map for transaction
	 */
	private void transformToRollbackCommand(final CommandParam commandParam,
			final Map<Object, DedicatedAccountInfoVO> updatedParamMapForTransaction) {
		final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
		final List<CommandParam> dedicatedAccntInfoChildParams = new ArrayList<CommandParam>();
		for (final Map.Entry<Object, DedicatedAccountInfoVO> entry : updatedParamMapForTransaction.entrySet()) {
			final List<CommandParam> usageCounterArrayChildren = new ArrayList<CommandParam>();
			// Creating a struct type command param to wrap child objects
			final CommandParamInput commandParamInputStruct = new CommandParamInput(ParameterFeedType.INPUT, null);
			commandParamInputStruct.setType(CommandParameterType.STRUCT);
			commandParamInputStruct.setParent(commandParamInput);

			final DedicatedAccountInfoVO accountInfoVO = entry.getValue();
			final CommandParam dedicatedAccountID = accountInfoVO.getDedicatedAccountID();
			usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
					commandParamInputStruct, DEDICATED_ACCOUNT_ID, Primitives.INTEGER, dedicatedAccountID.getValue()));

			final CommandParam dedicatedAccountUnitType = accountInfoVO.getDedicatedAccountUnitType();
			if (null != dedicatedAccountUnitType) {
				usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
						commandParamInputStruct, DEDICATED_ACCOUNT_UNIT_TYPE, Primitives.INTEGER,
						dedicatedAccountUnitType.getValue()));
			}
			final CommandParam startDate = accountInfoVO.getStartDate();
			if (null != startDate) {
				usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
						commandParamInputStruct, START_DATE, Primitives.DATETIME, DateUtil.convertCalendarDateToString(
								(Calendar) startDate.getValue(), FDPConstant.FDP_DB_SAVE_DATE_PATTERN)));
			}
			final CommandParam expiryDate = accountInfoVO.getExpiryDate();
			if (null != expiryDate) {
				usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
						commandParamInputStruct, EXPIRY_DATE, Primitives.DATETIME, DateUtil
								.convertCalendarDateToString((Calendar) expiryDate.getValue(),
										FDPConstant.FDP_DB_SAVE_DATE_PATTERN)));
			}
			final CommandParam updateAction = accountInfoVO.getUpdateAction();
			if (null != updateAction) {
				usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
						commandParamInputStruct, UPDATE_ACTION, Primitives.STRING, updateAction.getValue()));
			}
			commandParamInputStruct.setChilderen(usageCounterArrayChildren);
			dedicatedAccntInfoChildParams.add(commandParamInputStruct);
		}
		commandParamInput.setChilderen(dedicatedAccntInfoChildParams);
	}

	/**
	 * The Class DedicatedAccountInfoVO.
	 */
	class DedicatedAccountInfoVO {
		CommandParam dedicatedAccountID;
		CommandParam dedicatedAccountUnitType;
		CommandParam startDate;
		CommandParam expiryDate;
		CommandParam updateAction;

		/**
		 * Instantiates a new dedicated account info vo.
		 * 
		 * @param dedicatedAccountID
		 *            the dedicated account id
		 * @param updateAction
		 *            the update action
		 */
		public DedicatedAccountInfoVO(final CommandParam dedicatedAccountID, final CommandParam updateAction) {
			this.dedicatedAccountID = dedicatedAccountID;
			this.updateAction = updateAction;
		}

		/**
		 * Instantiates a new dedicated account info vo.
		 * 
		 * @param dedicatedAccountID
		 *            the dedicated account id
		 * @param dedicatedAccountUnitType
		 *            the dedicated account unit type
		 * @param startDate
		 *            the start date
		 * @param expiryDate
		 *            the expiry date
		 */
		public DedicatedAccountInfoVO(final CommandParam dedicatedAccountID,
				final CommandParam dedicatedAccountUnitType, final CommandParam startDate, final CommandParam expiryDate) {
			this.dedicatedAccountID = dedicatedAccountID;
			this.dedicatedAccountUnitType = dedicatedAccountUnitType;
			this.startDate = startDate;
			this.expiryDate = expiryDate;
		}

		/**
		 * @return the dedicatedAccountID
		 */
		public CommandParam getDedicatedAccountID() {
			return dedicatedAccountID;
		}

		/**
		 * @param dedicatedAccountID
		 *            the dedicatedAccountID to set
		 */
		public void setDedicatedAccountID(final CommandParam dedicatedAccountID) {
			this.dedicatedAccountID = dedicatedAccountID;
		}

		/**
		 * @return the dedicatedAccountUnitType
		 */
		public CommandParam getDedicatedAccountUnitType() {
			return dedicatedAccountUnitType;
		}

		/**
		 * @param dedicatedAccountUnitType
		 *            the dedicatedAccountUnitType to set
		 */
		public void setDedicatedAccountUnitType(final CommandParam dedicatedAccountUnitType) {
			this.dedicatedAccountUnitType = dedicatedAccountUnitType;
		}

		/**
		 * @return the startDate
		 */
		public CommandParam getStartDate() {
			return startDate;
		}

		/**
		 * @param startDate
		 *            the startDate to set
		 */
		public void setStartDate(final CommandParam startDate) {
			this.startDate = startDate;
		}

		/**
		 * @return the expiryDate
		 */
		public CommandParam getExpiryDate() {
			return expiryDate;
		}

		/**
		 * @param expiryDate
		 *            the expiryDate to set
		 */
		public void setExpiryDate(final CommandParam expiryDate) {
			this.expiryDate = expiryDate;
		}

		/**
		 * @return the updateAction
		 */
		public CommandParam getUpdateAction() {
			return updateAction;
		}

		/**
		 * @param updateAction
		 *            the updateAction to set
		 */
		public void setUpdateAction(final CommandParam updateAction) {
			this.updateAction = updateAction;
		}
	}
}
