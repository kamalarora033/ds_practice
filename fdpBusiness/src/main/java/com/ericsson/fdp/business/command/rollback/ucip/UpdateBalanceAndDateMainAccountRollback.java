package com.ericsson.fdp.business.command.rollback.ucip;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ParamTransformationType;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.TransformationUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of updateBalanceAndDate command.
 * 
 * @author Ericsson
 */
public class UpdateBalanceAndDateMainAccountRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -261813164131417601L;

	/**
	 * Instantiates a new update balance and date rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateBalanceAndDateMainAccountRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}
	
	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			try {
				final FDPCommand executedCommand = (FDPCommand) otherParams[0];
				//Added by Rajeev
				if (executedCommand.getCommandDisplayName().equals(Command.UPDATE_BALACEANDATE.getCommandName())) {
					Object skipCharging = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SKIP_CHARGING);
					if (skipCharging != null && (Boolean)skipCharging) {
						return Status.SUCCESS;
					}
				}
				if(executedCommand.getInputParam(ChargingUtil.ADJUSTMENT_AMOUNT_VALUE) != null){
					final CommandParamInput commandParamInput = (CommandParamInput) executedCommand.getInputParam(ChargingUtil.ADJUSTMENT_AMOUNT_VALUE);
					commandParamInput.setValue(TransformationUtil.evaluateTransformation(Long.valueOf(commandParamInput.getValue().toString()),
							ParamTransformationType.NEGATIVE));
					this.setInputParam(executedCommand.getInputParam());
				}
				if(executedCommand.getInputParam(ChargingUtil.DEDICATED_ACCOUNT_UPDATE_INFORMATION) != null){
					final CommandParam daChildNodes = executedCommand.getInputParam(ChargingUtil.DEDICATED_ACCOUNT_UPDATE_INFORMATION);
					if(daChildNodes != null && daChildNodes instanceof CommandParamInput && daChildNodes.getChilderen() != null && null != daChildNodes.getChilderen().get(0)) {
						CommandParamInput daChildNodesInput = (CommandParamInput)daChildNodes;
						for(CommandParam daChildNode : daChildNodesInput.getChilderen()){
							if(daChildNode instanceof CommandParamInput && daChildNode.getChilderen()!=null){
								for(CommandParam daChildNodeParam : daChildNode.getChilderen()){
									if(daChildNodeParam instanceof CommandParamInput && ChargingUtil.ADJUSTMENT_AMOUNT_VALUE.equals(daChildNodeParam.getName())) {
										CommandParamInput daChildNodeParamInput = (CommandParamInput) daChildNodeParam;
										daChildNodeParamInput.setValue(TransformationUtil.evaluateTransformation(Long.valueOf(daChildNodeParamInput.getValue().toString()),
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
}
