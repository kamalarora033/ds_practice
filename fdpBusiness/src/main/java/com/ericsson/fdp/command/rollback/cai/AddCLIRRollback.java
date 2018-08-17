package com.ericsson.fdp.command.rollback.cai;

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

public class AddCLIRRollback extends NonTransactionCommand {

	private final String ACTIONTYPE = "ACTIONTYPE,MODCLIR:PROV_COMM";

	public AddCLIRRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {

		final FDPCacheable cachedCommand = ApplicationConfigUtil
				.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(),
								ModuleType.COMMAND, Command.REMOVECLIRSERVICE
										.getCommandDisplayName()));

		CommandParam commandParamFromDB = this.getInputParam(ACTIONTYPE);
		AbstractCommand removeClirService = (AbstractCommand) cachedCommand;

		if (otherParams != null && otherParams[0] != null
				&& otherParams[0] instanceof FDPCommand) {

			final int removeSize = removeClirService.getInputParam().size();
			for (int j = 0; j < removeSize; j++) {
				if (removeClirService.getInputParam().get(j).getName()
						.equals(ACTIONTYPE)) {
					((CommandParamInput) (removeClirService.getInputParam()
							.get(j)))
							.setValue(((CommandParamInput) commandParamFromDB)
									.getDefinedValue());
					
				}
			}
		}
		return CommandUtil.executeCommand(fdpRequest, removeClirService, true);
	}
}
