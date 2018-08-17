package com.ericsson.fdp.business.command.rollback.ucip;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.command.rollback.helper.CommandRollbackHelper;
import com.ericsson.fdp.business.util.ChargingUtil;
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
 * This class is used to implement rollback of updateFaFList command.
 * 
 * @author Ericsson
 */
public class UpdateFaFListRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8825200782881697154L;

	/** The faf information. */
	private final String FAF_INFORMATION_LIST = "fafInformationList";
	/** The faf indicator. */
	private final String FAF_INDICATOR = "fafIndicator";
	/** The owner. */
	private final String OWNER = "owner";
	/** The faf number. */
	private final String FAF_NUMBER = "fafNumber";
	/** The faf action. */
	private final String FAF_ACTION = "fafAction";
	/** The faf action add. */
	private final String FAF_ACTION_ADD = "ADD";
	/** The faf action delete. */
	private final String FAF_ACTION_DELETE = "DELETE";
	/** The faf action set. */
	private final String FAF_ACTION_SET = "SET";
	/** The dot separator. */
	private final String DOT_SEPARATOR = FDPConstant.PARAMETER_SEPARATOR;

	/**
	 * Instantiates a new update fa f list.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateFaFListRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		
		
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			
		
		try {
			// extracting command parameters from two source commands
			//extractionFromSourceCommands(fdpRequest, otherParams);
			//Execute UpdateFAF Rollback command and update action input. 
			final FDPCommand executedCommand = (FDPCommand) otherParams[0];
			if(executedCommand.getInputParam(FDPConstant.FAFACTION) != null){
				final CommandParamInput commandParamInput = (CommandParamInput) executedCommand.getInputParam(FDPConstant.FAFACTION);
				if(FDPConstant.FAF_ACTION_ADD.equalsIgnoreCase(executedCommand.getInputParam(FDPConstant.FAFACTION).getValue().toString())){
					commandParamInput.setValue(FDPConstant.FAF_ACTION_DELETE);
				}
				this.setInputParam(executedCommand.getInputParam());
			}
			return executeCommand(fdpRequest);
		} catch (final EvaluationFailedException e) {
			throw new ExecutionFailedException("Could not execute command", e);
		}
		}else{
			throw new ExecutionFailedException("Could not find executed command");
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
	 *             the execution failed exception
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams)
			throws ExecutionFailedException, EvaluationFailedException {
		boolean fafInformationFound = false;
		boolean fafActionFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();

			while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (FAF_ACTION.equals(commandParam.getName())) {
				fafActionFound = updateFaFAction(commandParam, fdpRequest, otherParams != null ? otherParams[0] : null);
				if (!fafActionFound) {
					commandParamIterator.remove();
				}
			} else if (FAF_INFORMATION_LIST.equals(commandParam.getName())) {
				fafInformationFound = updateFaFInformation(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!fafInformationFound) {
					commandParamIterator.remove();
				}
			} else if (commandParam instanceof CommandParamInput) {
				final CommandParamInput input = (CommandParamInput) commandParam;
				input.evaluateValue(fdpRequest);
			}
		}
		if (!fafActionFound && !fafInformationFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find FaF information");
		}

	}

	/**
	 * Update faf action.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParam
	 *            the other param
	 * @return true, if successful
	 */
	private boolean updateFaFAction(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) {
		boolean isPresent = false;
		if (commandParam instanceof CommandParamInput) {
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				final CommandParam commandParamForFaFAction = executedUpdateCommand.getInputParam(FAF_ACTION);
				if (null == commandParamForFaFAction) {
					return isPresent;
				}
				final String fafAction = (String) commandParamForFaFAction.getValue();
				final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
				commandParamInput.setType(CommandParameterType.PRIMITIVE);
				commandParamInput.setPrimitiveValue(Primitives.STRING);
				if (FAF_ACTION_ADD.equals(fafAction)) {
					commandParamInput.setValue(FAF_ACTION_DELETE);
				} else if (FAF_ACTION_DELETE.equals(fafAction)) {
					commandParamInput.setValue(FAF_ACTION_ADD);
				} else {
					commandParamInput.setValue(FAF_ACTION_SET);
				}
				isPresent = true;
			}
		}
		return isPresent;
	}

	/**
	 * Update faf information.
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
	private boolean updateFaFInformation(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		boolean isPresent = false;
		final List<FaFInfoVO> commandParamsFromGetCommand = new ArrayList<FaFInfoVO>();
		final List<FaFInfoVO> commandParamsFromUpdateCommand = new ArrayList<FaFInfoVO>();
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
			int i = 0;
			while (fdpGetCommand.getOutputParam((flattenParam + DOT_SEPARATOR + i + DOT_SEPARATOR + FAF_INDICATOR)
					.toLowerCase()) != null) {
				final CommandParam fafIndicator = fdpGetCommand.getOutputParam((flattenParam + DOT_SEPARATOR + i
						+ DOT_SEPARATOR + FAF_INDICATOR).toLowerCase());
				final CommandParam fafNumber = fdpGetCommand.getOutputParam((flattenParam + DOT_SEPARATOR + i
						+ DOT_SEPARATOR + FAF_NUMBER).toLowerCase());
				final CommandParam owner = fdpGetCommand.getOutputParam((flattenParam + DOT_SEPARATOR + i
						+ DOT_SEPARATOR + OWNER).toLowerCase());
				commandParamsFromGetCommand.add(new FaFInfoVO(fafIndicator, fafNumber, owner));
				i = i + 1;
			}
			if (otherParam instanceof FDPCommand) {
				final FDPCommand executedUpdateCommand = (FDPCommand) otherParam;
				final CommandParam commandParamForFaFInformation = executedUpdateCommand
						.getInputParam(FAF_INFORMATION_LIST);
				final List<CommandParam> fafInformationChildren = commandParamForFaFInformation.getChilderen();
				for (final CommandParam child : fafInformationChildren) {
					final List<CommandParam> commandParamsList = child.getChilderen();
					final FaFInfoVO fafListVO = new FaFInfoVO();
					for (final CommandParam commandParamObject : commandParamsList) {
						if (FAF_INDICATOR.equals(commandParamObject.getName())) {
							fafListVO.setFafIndicator(commandParamObject);
						} else if (FAF_NUMBER.equals(commandParamObject.getName())) {
							fafListVO.setFafNumber(commandParamObject);
						} else if (OWNER.equals(commandParamObject.getName())) {
							fafListVO.setOwner(commandParamObject);
						}
					}
					commandParamsFromUpdateCommand.add(fafListVO);
				}
			}
		}
		if (!commandParamsFromGetCommand.isEmpty() || !commandParamsFromUpdateCommand.isEmpty()) {
			final CommandParam fafActionCommandParam = this.getInputParam(FAF_ACTION);
			if (FAF_ACTION_SET.equals(fafActionCommandParam.getValue())) {
				transformToRollbackCommand(commandParam, commandParamsFromGetCommand);
			} else {
				transformToRollbackCommand(commandParam, commandParamsFromUpdateCommand);
			}
			isPresent = true;
		}
		return isPresent;
	}

	/**
	 * Transform to rollback command.
	 * 
	 * @param commandParam
	 *            the command param
	 * @param commandParamsFromGetCommand
	 *            the command params from get command
	 */
	private void transformToRollbackCommand(final CommandParam commandParam, final List<FaFInfoVO> commandParamsList) {
		final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
		final List<CommandParam> usageCounterChildParams = new ArrayList<CommandParam>();
		for (final FaFInfoVO fafInfoVO : commandParamsList) {
			final List<CommandParam> usageCounterArrayChildren = new ArrayList<CommandParam>();
			// Creating a struct type command param to wrap child objects
			final CommandParamInput commandParamInputStruct = new CommandParamInput(ParameterFeedType.INPUT, null);
			commandParamInputStruct.setType(CommandParameterType.STRUCT);
			commandParamInputStruct.setParent(commandParamInput);

			final CommandParam fafIndicator = fafInfoVO.getFafIndicator();
			final CommandParam fafNumber = fafInfoVO.getFafNumber();
			final CommandParam owner = fafInfoVO.getOwner();

			// Adding an id command param
			usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
					commandParamInputStruct, FAF_INDICATOR, Primitives.INTEGER, fafIndicator.getValue()));
			usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
					commandParamInputStruct, FAF_NUMBER, Primitives.STRING, fafNumber.getValue()));
			usageCounterArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(
					commandParamInputStruct, OWNER, Primitives.STRING, owner.getValue()));
			commandParamInputStruct.setChilderen(usageCounterArrayChildren);
			usageCounterChildParams.add(commandParamInputStruct);
		}
		commandParamInput.setChilderen(usageCounterChildParams);
	}

	class FaFInfoVO {
		CommandParam fafIndicator;
		CommandParam fafNumber;
		CommandParam owner;

		public FaFInfoVO() {
			super();
		}

		public FaFInfoVO(final CommandParam fafIndicator, final CommandParam fafNumber, final CommandParam owner) {
			this.fafIndicator = fafIndicator;
			this.fafNumber = fafNumber;
			this.owner = owner;
		}

		/**
		 * @return the fafIndicator
		 */
		public CommandParam getFafIndicator() {
			return fafIndicator;
		}

		/**
		 * @param fafIndicator
		 *            the fafIndicator to set
		 */
		public void setFafIndicator(final CommandParam fafIndicator) {
			this.fafIndicator = fafIndicator;
		}

		/**
		 * @return the fafNumber
		 */
		public CommandParam getFafNumber() {
			return fafNumber;
		}

		/**
		 * @param fafNumber
		 *            the fafNumber to set
		 */
		public void setFafNumber(final CommandParam fafNumber) {
			this.fafNumber = fafNumber;
		}

		/**
		 * @return the owner
		 */
		public CommandParam getOwner() {
			return owner;
		}

		/**
		 * @param owner
		 *            the owner to set
		 */
		public void setOwner(final CommandParam owner) {
			this.owner = owner;
		}
	}
}
