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
import com.ericsson.fdp.business.enums.ParamTransformationType;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.TransformationUtil;
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
 * This class is used to implement rollback of updateAccumulators command.
 * 
 * @author Ericsson
 */
public class UpdateAccumulatorsRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2838553622888404678L;
	/** The accumulator end date. */
	private final String ACCUMULATOR_END_DATE = "accumulatorEndDate";
	/** The accumulator start date. */
	private final String ACCUMULATOR_START_DATE = "accumulatorStartDate";
	/** The accumulator value. */
	private final String ACCUMULATOR_VALUE = "accumulatorValue";
	/** The accumulator information. */
	private final String ACCUMULATOR_INFORMATION = "accumulatorInformation";
	/** The accumulator update information. */
	private final String ACCUMULATOR_UPDATE_INFORMATION = "accumulatorUpdateInformation";
	/** The accumulator id. */
	private final String ACCUMULATOR_ID = "accumulatorID";
	/** The Constant ACCUMULATOR_VALUE_ABSOLUTE. */
	private final String ACCUMULATOR_VALUE_ABSOLUTE = "accumulatorValueAbsolute";
	/** The Constant ACCUMULATOR_VALUE_RELATIVE. */
	private final String ACCUMULATOR_VALUE_RELATIVE = "accumulatorValueRelative";
	/** The dot separator. */
	private final String DOT_SEPARATOR = FDPConstant.PARAMETER_SEPARATOR;

	/**
	 * Instantiates a new update accumulators rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateAccumulatorsRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	/*@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		try {
			// extracting command parameters from two source commands
			extractionFromSourceCommands(fdpRequest, otherParams);
			return executeCommand(fdpRequest);
		} catch (final EvaluationFailedException e) {
			throw new ExecutionFailedException("Could not execute command", e);
		}
	}*/

	
	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			try {
				final FDPCommand executedCommand = (FDPCommand) otherParams[0];
				if(executedCommand.getInputParam(ACCUMULATOR_UPDATE_INFORMATION) != null){
					final CommandParam accUpdateInfoParentNode = executedCommand.getInputParam(ACCUMULATOR_UPDATE_INFORMATION);
					if(accUpdateInfoParentNode != null && accUpdateInfoParentNode instanceof CommandParamInput && accUpdateInfoParentNode.getChilderen() != null && null != accUpdateInfoParentNode.getChilderen().get(0)) {
						CommandParamInput accUpdateInfoParentNodeInput = (CommandParamInput)accUpdateInfoParentNode;
						for(CommandParam accInfoChildNode : accUpdateInfoParentNodeInput.getChilderen()){
							if(accInfoChildNode instanceof CommandParamInput && accInfoChildNode.getChilderen()!=null){
								for(CommandParam accInfoChildNodeParam : accInfoChildNode.getChilderen()){
									if(accInfoChildNodeParam instanceof CommandParamInput && ACCUMULATOR_VALUE_RELATIVE.equals(accInfoChildNodeParam.getName())) {
										CommandParamInput accInfoChildNodeParamInput = (CommandParamInput) accInfoChildNodeParam;
										accInfoChildNodeParamInput.setValue(TransformationUtil.evaluateTransformation(Long.valueOf(accInfoChildNodeParamInput.getValue().toString()),
												ParamTransformationType.NEGATIVE));
									}		
								}
							}
						}
					}
					this.setInputParam(executedCommand.getInputParam());
				}
				return executeCommand(fdpRequest);
			}catch (final EvaluationFailedException e) {
				throw new ExecutionFailedException("Could not execute command", e);
			}
		}else {
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
	 */
	private void extractionFromSourceCommands(final FDPRequest fdpRequest, final Object[] otherParams)
			throws ExecutionFailedException {
		boolean accumulatorUpdateInformationFound = false;
		final Iterator<CommandParam> commandParamIterator = getInputParam().iterator();
		while (commandParamIterator.hasNext()) {
			final CommandParam commandParam = commandParamIterator.next();
			if (ACCUMULATOR_UPDATE_INFORMATION.equals(commandParam.getName())) {
				accumulatorUpdateInformationFound = updateAccumulatorUpdateInformation(commandParam, fdpRequest,
						otherParams != null ? otherParams[0] : null);
				if (!accumulatorUpdateInformationFound) {
					commandParamIterator.remove();
				}
			}
		}
		if (!accumulatorUpdateInformationFound) {
			// Could not find any of the required parameter.
			throw new ExecutionFailedException("Could not find accumulator Update Information");
		}
	}

	/**
	 * Update usage accumulator update information.
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
	private boolean updateAccumulatorUpdateInformation(final CommandParam commandParam, final FDPRequest fdpRequest,
			final Object otherParam) throws ExecutionFailedException {
		boolean isPresent = false;
		final Map<Object, AccumulatorInfoVO> updatedParamMapForTransaction = new LinkedHashMap<Object, AccumulatorInfoVO>();
		if (commandParam instanceof CommandParamInput) {
			final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
			final CommandParamOutput paramOutput = (CommandParamOutput) commandParamInput.getDefinedValue();
			final FDPCommand fdpGetCommand = fdpRequest.getExecutedCommand(paramOutput.getCommand()
					.getCommandDisplayName());
			if (null == fdpGetCommand) {
				throw new ExecutionFailedException("The dependent command "
						+ paramOutput.getCommand().getCommandDisplayName() + " was not executed");
			}
			// Creating a map to maintain the key-value pair of parameterId and
			// commandparam object
			int i = 0;
			// Iterating till the output param from the get command is not null
			while (fdpGetCommand
					.getOutputParam((ACCUMULATOR_INFORMATION + DOT_SEPARATOR + i + DOT_SEPARATOR + ACCUMULATOR_ID)
							.toLowerCase()) != null) {
				final CommandParam accumulatorID = fdpGetCommand.getOutputParam((ACCUMULATOR_INFORMATION
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + ACCUMULATOR_ID).toLowerCase());
				final CommandParam accumulatorValue = fdpGetCommand.getOutputParam((ACCUMULATOR_INFORMATION
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + ACCUMULATOR_VALUE).toLowerCase());
				final CommandParam accumulatorStartDate = fdpGetCommand.getOutputParam((ACCUMULATOR_INFORMATION
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + ACCUMULATOR_START_DATE).toLowerCase());
				final CommandParam accumulatorEndDate = fdpGetCommand.getOutputParam((ACCUMULATOR_INFORMATION
						+ DOT_SEPARATOR + i + DOT_SEPARATOR + ACCUMULATOR_END_DATE).toLowerCase());
				updatedParamMapForTransaction.put(accumulatorID.getValue(), new AccumulatorInfoVO(accumulatorID,
						accumulatorValue, accumulatorStartDate, accumulatorEndDate));
				i = i + 1;
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
			final Map<Object, AccumulatorInfoVO> updatedParamMapForTransaction) {
		final CommandParamInput commandParamInput = (CommandParamInput) commandParam;
		final List<CommandParam> accumulatorInfoChildParams = new ArrayList<CommandParam>();
		for (final Map.Entry<Object, AccumulatorInfoVO> entry : updatedParamMapForTransaction.entrySet()) {
			final List<CommandParam> acctInfoArrayChildren = new ArrayList<CommandParam>();
			// Creating a struct type command param to wrap child objects
			final CommandParamInput commandParamInputStruct = new CommandParamInput(ParameterFeedType.INPUT, null);
			commandParamInputStruct.setType(CommandParameterType.STRUCT);
			commandParamInputStruct.setParent(commandParamInput);

			final AccumulatorInfoVO accumulatorInfoVO = entry.getValue();
			final CommandParam accumulatorID = accumulatorInfoVO.getAccumulatorID();

			acctInfoArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(commandParamInputStruct,
					ACCUMULATOR_ID, Primitives.INTEGER, accumulatorID.getValue()));
			acctInfoArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(commandParamInputStruct,
					ACCUMULATOR_VALUE_ABSOLUTE, Primitives.INTEGER, accumulatorInfoVO.getAccumulatorValue()));
			acctInfoArrayChildren.add(CommandRollbackHelper.getCommandInputObjectForPrimitive(commandParamInputStruct,
					ACCUMULATOR_START_DATE, Primitives.DATETIME, accumulatorInfoVO.getAccumulatorStartDate()));

			commandParamInputStruct.setChilderen(acctInfoArrayChildren);
			accumulatorInfoChildParams.add(commandParamInputStruct);
		}
		commandParamInput.setChilderen(accumulatorInfoChildParams);
	}

	/**
	 * The Class AccumulatorInfo.
	 */
	class AccumulatorInfoVO {
		CommandParam accumulatorID;
		CommandParam accumulatorValue;
		CommandParam accumulatorStartDate;
		CommandParam accumulatorEndDate;

		/**
		 * Instantiates a new accumulator info.
		 * 
		 * @param accumulatorID
		 *            the accumulator id
		 * @param accumulatorValue
		 *            the accumulator value
		 */
		public AccumulatorInfoVO(final CommandParam accumulatorID, final CommandParam accumulatorValue) {
			this.accumulatorID = accumulatorID;
			this.accumulatorValue = accumulatorValue;
		}

		/**
		 * Instantiates a new accumulator info.
		 * 
		 * @param accumulatorID
		 *            the accumulator id
		 * @param accumulatorValue
		 *            the accumulator value
		 * @param accumulatorStartDate
		 *            the accumulator start date
		 * @param accumulatorEndDate
		 *            the accumulator end date
		 */
		public AccumulatorInfoVO(final CommandParam accumulatorID, final CommandParam accumulatorValue,
				final CommandParam accumulatorStartDate, final CommandParam accumulatorEndDate) {
			this.accumulatorID = accumulatorID;
			this.accumulatorValue = accumulatorValue;
			this.accumulatorStartDate = accumulatorStartDate;
			this.accumulatorEndDate = accumulatorEndDate;
		}

		/**
		 * @return the accumulatorID
		 */
		public CommandParam getAccumulatorID() {
			return accumulatorID;
		}

		/**
		 * @param accumulatorID
		 *            the accumulatorID to set
		 */
		public void setAccumulatorID(final CommandParam accumulatorID) {
			this.accumulatorID = accumulatorID;
		}

		/**
		 * @return the accumulatorValue
		 */
		public CommandParam getAccumulatorValue() {
			return accumulatorValue;
		}

		/**
		 * @param accumulatorValue
		 *            the accumulatorValue to set
		 */
		public void setAccumulatorValue(final CommandParam accumulatorValue) {
			this.accumulatorValue = accumulatorValue;
		}

		/**
		 * @return the accumulatorStartDate
		 */
		public CommandParam getAccumulatorStartDate() {
			return accumulatorStartDate;
		}

		/**
		 * @param accumulatorStartDate
		 *            the accumulatorStartDate to set
		 */
		public void setAccumulatorStartDate(final CommandParam accumulatorStartDate) {
			this.accumulatorStartDate = accumulatorStartDate;
		}

		/**
		 * @return the accumulatorEndDate
		 */
		public CommandParam getAccumulatorEndDate() {
			return accumulatorEndDate;
		}

		/**
		 * @param accumulatorEndDate
		 *            the accumulatorEndDate to set
		 */
		public void setAccumulatorEndDate(final CommandParam accumulatorEndDate) {
			this.accumulatorEndDate = accumulatorEndDate;
		}
	}
}
