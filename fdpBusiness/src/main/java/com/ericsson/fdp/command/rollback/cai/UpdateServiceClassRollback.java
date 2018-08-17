package com.ericsson.fdp.command.rollback.cai;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public class UpdateServiceClassRollback extends NonTransactionCommand{
	
private static final long serialVersionUID = 88252007828816989L;
	
	private static final String SERVICE_CLASS_CURRENT = "serviceClassCurrent";

	private static final String SERVICE_CLASS_NEW = "serviceClassNew";
	
	public UpdateServiceClassRollback(String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest,final Object... otherParams) throws ExecutionFailedException {
		
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			try {
				final FDPCommand executedCommand = (FDPCommand) otherParams[0];
				CommandParamInput commandParamInputNew = null;
				CommandParamInput commandParamInputCurrent = null;
				Object serviceClassCurrent = null;
				Object serviceClassNew = null;
				
				if(executedCommand.getInputParam(SERVICE_CLASS_NEW) != null){
					commandParamInputNew = (CommandParamInput) executedCommand.getInputParam(SERVICE_CLASS_NEW);
					serviceClassNew = commandParamInputNew.getValue();
				}
				
				if(executedCommand.getInputParam(SERVICE_CLASS_CURRENT) != null){
					commandParamInputCurrent = (CommandParamInput) executedCommand.getInputParam(SERVICE_CLASS_CURRENT);
					serviceClassCurrent = commandParamInputCurrent.getValue();
				}
				if (serviceClassNew != null) {
					CommandParamInput commandParamInput = (CommandParamInput) executedCommand.getInputParam(SERVICE_CLASS_CURRENT);
					commandParamInput.setValue(serviceClassNew);
				}
				
				if (serviceClassCurrent != null) {
					CommandParamInput commandParamInput = (CommandParamInput) executedCommand.getInputParam(SERVICE_CLASS_NEW);
					commandParamInput.setValue(serviceClassCurrent);
				}
				
				this.setInputParam(executedCommand.getInputParam());
				
				return executeCommand(fdpRequest);
			}catch (final EvaluationFailedException e) {
				throw new ExecutionFailedException("Could not execute command", e);
			}
		}else {
			throw new ExecutionFailedException("Could not find executed command");
		}
	}
	
}
