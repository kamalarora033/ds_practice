package com.ericsson.fdp.command.rollback.cai;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

public class AddUnlimitedDataBundleRollback extends NonTransactionCommand {
	
	private static final long serialVersionUID = 1L;
	
	private final String SERVNAME_COMM="SERVNAME_COMM";

	public AddUnlimitedDataBundleRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException{

		FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache()
				.getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.REMOVEUNLIMITEDDATABUNDLE.getCommandDisplayName()));

		AbstractCommand removeUnlimitedBundle = (AbstractCommand) cachedCommand;
		final FDPCommand addUnlimitedBundle = fdpRequest.getExecutedCommand(FDPConstant.ADD_UNLIMITED_DATA_BUNDLE);
		
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			List<CommandParam> commandinputparam = addUnlimitedBundle.getInputParam();
			
			final int addSize=commandinputparam.size();
			final int removeSize=removeUnlimitedBundle.getInputParam().size();

			for (int i = 0; i < addSize; i++) {
				if (commandinputparam.get(i).getName().equals(SERVNAME_COMM)) {
					for (int j = 0; j < removeSize; j++) {
						if (removeUnlimitedBundle.getInputParam().get(j).getName().equals(SERVNAME_COMM)) {
							final CommandParam addCommandParam=commandinputparam.get(i);
							removeUnlimitedBundle.getInputParam().set(j, addCommandParam);
							//System.out.println("Input Param for Remove Unlimited data bundle:" + removeUnlimitedBundle.getInputParam());
						}
					}

				}
			}
			
		}
		return CommandUtil.executeCommand(fdpRequest, removeUnlimitedBundle, true);
	}
}

