package com.ericsson.fdp.command.rollback.cai;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class LYTClaimRedemptionRollBack extends NonTransactionCommand {

	private static final long serialVersionUID = 8825200782881698989L;
	
	private static final String TRANS_NUM = "TransNum";

	public LYTClaimRedemptionRollBack(String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest,final Object... otherParams) throws ExecutionFailedException {
		final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
                new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.LOYALTY_REDEEM_ROLLBACK.getCommandDisplayName()));
		AbstractCommand LYTrollback = (AbstractCommand) cachedCommand;
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
				final FDPCommand executedCommand = (FDPCommand) otherParams[0];
					if (executedCommand.getCommandName().equals(Command.LOYALTY_REDEEM.getCommandDisplayName())) {
						List<CommandParam> commandinputparam = executedCommand.getInputParam();
						for (int i = 0; i < commandinputparam.size(); i++) {
							CommandParamInput commandParam = (CommandParamInput) commandinputparam.get(i);
							if (commandParam.getName().equals(TRANS_NUM)) {
								CommandParamInput rollbackcommandParam = (CommandParamInput)LYTrollback.getInputParam().get(i);
								rollbackcommandParam.setValue(commandParam.getValue());
							}
						}
					}
				LYTrollback.setRollbackCommand(true);
				return CommandUtil.executeCommand(fdpRequest, LYTrollback, true);
		} else {
			throw new ExecutionFailedException("Could not execute command");
		}
	}
}