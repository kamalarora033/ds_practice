package com.ericsson.fdp.business.command.rollback.acip;

import java.util.ArrayList;
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
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This class is used to implement rollback of
 * updatePeriodicAccountManagementData command.
 * 
 * @author Ericsson
 */
public class UpdatePeriodicAccountManagementDataRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 968812221013474136L;
	/** The dot separator. */
	private final String DOT_SEPARATOR = FDPConstant.PARAMETER_SEPARATOR;
	/** The schedule id. */
	private final String SCHEDULE_ID = "scheduleID";
	/** The pam class id. */
	private final String PAM_CLASS_ID = "pamClassID";
	/** The pam class id new. */
	private final String PAM_CLASS_ID_NEW = "pamClassIDNew";
	/** The pam class id old. */
	private final String PAM_CLASS_ID_OLD = "pamClassIDOld";
	/** The pam service id. */
	private final String PAM_SERVICE_ID = "pamServiceID";
	/** The pam information list. */
	private final String PAM_INFORMATION_LIST = "pamInformationList";
	/** The pam update information list. */
	private final String PAM_UPDATE_INFORMATION_LIST = "pamUpdateInformationList";

	public UpdatePeriodicAccountManagementDataRollback(final String commandDisplayName) {
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
		boolean pamUpdateInformationListFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (PAM_UPDATE_INFORMATION_LIST.equals(commandParam.getName())) {
				pamUpdateInformationListFound = updatePamUpdateInformation(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!pamUpdateInformationListFound) {
					commandParamIterator.remove();
				}
			}
		}
		if (!pamUpdateInformationListFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find pam Update Information List");
		}
	}

	/**
	 * Update pam update information.
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
	private boolean updatePamUpdateInformation(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		boolean isPresent = false;
		final Map<Object, PAMInfoVO> updatedParamMapForTransaction = new LinkedHashMap<Object, PAMInfoVO>();
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
			int i = 0;
			final Map<Object, PAMInfoVO> commandParamMap = new LinkedHashMap<Object, PAMInfoVO>();
			// Iterating till the output param from the get command is not null
			while (fdpGetCommand
					.getOutputParam((PAM_INFORMATION_LIST + DOT_SEPARATOR + i + DOT_SEPARATOR + PAM_SERVICE_ID)
							.toLowerCase()) != null) {
				final CommandParam pamServiceID = fdpGetCommand.getOutputParam((PAM_INFORMATION_LIST + DOT_SEPARATOR
						+ i + DOT_SEPARATOR + PAM_SERVICE_ID).toLowerCase());
				final CommandParam pamClassID = fdpGetCommand.getOutputParam((PAM_INFORMATION_LIST + DOT_SEPARATOR + i
						+ DOT_SEPARATOR + PAM_CLASS_ID).toLowerCase());
				final CommandParam scheduleID = fdpGetCommand.getOutputParam((PAM_INFORMATION_LIST + DOT_SEPARATOR + i
						+ DOT_SEPARATOR + SCHEDULE_ID).toLowerCase());

				final PAMInfoVO pamInfoVO = new PAMInfoVO();
				pamInfoVO.setPamServiceID(pamServiceID);
				pamInfoVO.setPamClassIDNew(pamClassID);
				pamInfoVO.setScheduleID(scheduleID);

				commandParamMap.put(pamServiceID.getValue(), pamInfoVO);
				i = i + 1;
			}
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				// Getting the command param for counter usage information
				// element from the update command fired
				final CommandParam pamUpdateInfoParam = executedUpdateCommand
						.getInputParam(PAM_UPDATE_INFORMATION_LIST);
				final List<CommandParam> pamInfoChildren = pamUpdateInfoParam.getChilderen();
				// Iterating on children to find the usageCounterID element from
				// the command params
				for (final CommandParam child : pamInfoChildren) {
					final List<CommandParam> commandParams = child.getChilderen();
					PAMInfoVO pamInfoVO = null;
					Object pamServiceID = null;
					for (final CommandParam commandParamObject : commandParams) {
						final String paramName = commandParamObject.getName();
						final Object commandParamValue = commandParamObject.getValue();
						if (PAM_SERVICE_ID.equals(paramName)) {
							pamServiceID = commandParamValue;
							pamInfoVO = commandParamMap.get(commandParamValue);
						} else if (PAM_CLASS_ID_NEW.equals(paramName)) {
							if (null != pamInfoVO) {
								pamInfoVO.setPamClassIDOld(commandParamObject);
							}
							updatedParamMapForTransaction.put(pamServiceID, pamInfoVO);
							break;
						}
					}
				}
			} else {
				throw new ExecutionFailedException("");
			}
		}
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
			final Map<Object, PAMInfoVO> updatedParamMapForTransaction) {
		final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
		final List<CommandParam> pamInfoChildParams = new ArrayList<CommandParam>();
		for (final Map.Entry<Object, PAMInfoVO> entry : updatedParamMapForTransaction.entrySet()) {
			final List<CommandParam> pamInfoArrayChildren = new ArrayList<CommandParam>();
			final CommandParamInput commandParamInputStruct = new CommandParamInput(ParameterFeedType.INPUT, null);
			commandParamInputStruct.setType(CommandParameterType.STRUCT);
			commandParamInputStruct.setParent(commandParamInput);

			final PAMInfoVO infoVO = entry.getValue();
			final CommandParam pamServiceID = infoVO.getPamServiceID();
			pamInfoArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(commandParamInputStruct,
					PAM_SERVICE_ID, Primitives.INTEGER, pamServiceID.getValue()));
			final CommandParam pamClassIDOld = infoVO.getPamClassIDOld();
			pamInfoArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(commandParamInputStruct,
					PAM_CLASS_ID_OLD, Primitives.INTEGER, pamClassIDOld.getValue()));
			final CommandParam pamClassIDNew = infoVO.getPamClassIDNew();
			pamInfoArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(commandParamInputStruct,
					PAM_CLASS_ID_NEW, Primitives.INTEGER, pamClassIDNew.getValue()));

			commandParamInputStruct.setChilderen(pamInfoArrayChildren);
			pamInfoChildParams.add(commandParamInputStruct);
		}
		commandParamInput.setChilderen(pamInfoChildParams);
	}

	class PAMInfoVO {
		CommandParam pamServiceID;
		CommandParam pamClassIDOld;
		CommandParam pamClassIDNew;
		CommandParam scheduleID;

		/**
		 * @return the pamServiceID
		 */
		public CommandParam getPamServiceID() {
			return pamServiceID;
		}

		/**
		 * @param pamServiceID
		 *            the pamServiceID to set
		 */
		public void setPamServiceID(final CommandParam pamServiceID) {
			this.pamServiceID = pamServiceID;
		}

		/**
		 * @return the pamClassIDOld
		 */
		public CommandParam getPamClassIDOld() {
			return pamClassIDOld;
		}

		/**
		 * @param pamClassIDOld
		 *            the pamClassIDOld to set
		 */
		public void setPamClassIDOld(final CommandParam pamClassIDOld) {
			this.pamClassIDOld = pamClassIDOld;
		}

		/**
		 * @return the pamClassIDNew
		 */
		public CommandParam getPamClassIDNew() {
			return pamClassIDNew;
		}

		/**
		 * @param pamClassIDNew
		 *            the pamClassIDNew to set
		 */
		public void setPamClassIDNew(final CommandParam pamClassIDNew) {
			this.pamClassIDNew = pamClassIDNew;
		}

		/**
		 * @return the scheduleID
		 */
		public CommandParam getScheduleID() {
			return scheduleID;
		}

		/**
		 * @param scheduleID
		 *            the scheduleID to set
		 */
		public void setScheduleID(final CommandParam scheduleID) {
			this.scheduleID = scheduleID;
		}
	}
}
