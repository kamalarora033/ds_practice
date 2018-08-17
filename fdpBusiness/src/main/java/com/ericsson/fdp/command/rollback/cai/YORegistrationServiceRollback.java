package com.ericsson.fdp.command.rollback.cai;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public class YORegistrationServiceRollback extends NonTransactionCommand {

	public YORegistrationServiceRollback(final String commandDisplayName)
	{
		super(commandDisplayName);
	}
	
	
	@Override
	public Status execute(FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException
			{
		return CommandUtil.executeCommand(fdpRequest, Command.YO_DE_REGISTRATION, true);
		
			}
}
