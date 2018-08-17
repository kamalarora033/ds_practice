package com.ericsson.fdp.command.rollback.cai;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
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

public class AddBlackBerryRollBack extends NonTransactionCommand {

	private static final long serialVersionUID = 1L;

	private final String SERVNAME="PCRFSUB,SERVNAME_COL";

	public AddBlackBerryRollBack(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
		
		final FDPCacheable cachedCommand = ApplicationConfigUtil
				.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(),
								ModuleType.COMMAND,
								Command.REMOVEBLACKBERRYBUNDLE
										.getCommandDisplayName()));

		/*FDPCommand removeunlimiteddatabundle = (FDPCommand) cachedCommand;
		AbstractCommand ac = (AbstractCommand) cachedCommand;
		
		if (otherParams[0] != null) {
			if (cachedCommand instanceof FDPCommand) {
				
				setParamerters(ac, otherParams[0]);
				CommandUtil.executeCommand(fdpRequest,
						(FDPCommand) cachedCommand, true);
			} else {
				throw new ExecutionFailedException(
						"REMOVECLIRSERVICE Rollback Command failed");
			}
		}
		return CommandUtil.executeCommand(fdpRequest,
				ac, true);
	}

	private void setParamerters(AbstractCommand ac, Object object) {
		List<CommandParam> commandinputparams = ac.getInputParam();
		FDPCommand fdpCommand = (FDPCommand) object;

		List<CommandParam> inputparamlst = ac.getInputParam();

		for (int i = 0; i < inputparamlst.size(); i++) {
			if (inputparamlst.get(i).equals("SRVPNAME_COL")) {
				inputparamlst.set(i,
						fdpCommand.getInputParam("PCRFSUB,SERVNAME_col"));
			}
		}
*/	
		AbstractCommand removeBlackBerry = (AbstractCommand) cachedCommand;
		final FDPCommand addBlackBerry = fdpRequest.getExecutedCommand(FDPConstant.ADD_BLACKBERRY_BUNDLE);
		
		
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			List<CommandParam> commandinputparam = addBlackBerry.getInputParam();
			
			final int addSize=commandinputparam.size();
			final int removeSize=removeBlackBerry.getInputParam().size();

			for (int i = 0; i < addSize; i++) {
				if (commandinputparam.get(i).getName().equals(SERVNAME)) {
					for (int j = 0; j < removeSize; j++) {
						if (removeBlackBerry.getInputParam().get(j).getName().equals(this.getInputParam().get(0).getName())) {
							final CommandParam addCommandParam = commandinputparam.get(i);
							CommandParamInput param = (CommandParamInput) this.getInputParam().get(0);
							param.setValue(addCommandParam.getValue());
							removeBlackBerry.getInputParam().set(j, param);
							
						}
					}
					
				}
			}
			
		}
		return CommandUtil.executeCommand(fdpRequest, removeBlackBerry, true);
	}
}
