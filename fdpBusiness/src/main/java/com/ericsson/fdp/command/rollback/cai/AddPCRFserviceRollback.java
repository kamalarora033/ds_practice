package com.ericsson.fdp.command.rollback.cai;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.CommandParameterType;

public class AddPCRFserviceRollback extends NonTransactionCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6324648630652032209L;
	private static String TYPE_PARAMNAME = "TYPE_COL";
	private static String PREFIX_PARAMNAME = "Prefix_COL";
	private static String MSISDN_PARAMNAME = "MSISDN_COL";
	private static String SERVICENAME_PARAM = "SERVICE,DEF,SERVICENAME_COMM";
	private static String SERVICENAMEREPLACE_PARAM = "SERVPACKNAME";

	/**
	 *  
	 * @param commandDisplayName
	 *            the command display name
	 */
	public AddPCRFserviceRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}
	
	@Override
	public Status execute(FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {

		if (otherParams != null && otherParams[0] != null
				&& otherParams[0] instanceof FDPCommand) {
			final FDPCommand executedCommand = fdpRequest
					.getExecutedCommand(FDPConstant.ADD_PCRF_SEVICE);
			List<CommandParam> commandinputparam = executedCommand
					.getInputParam();

			List<CommandParam> pcrfremoveParam = new ArrayList<CommandParam>();
			setPCRFParam(pcrfremoveParam, executedCommand, commandinputparam);
			this.setInputParam(pcrfremoveParam);
		}
		return super.execute(fdpRequest, otherParams);
	}

	private void setPCRFParam(List<CommandParam> pcrfremoveParam,
			FDPCommand executedCommand, List<CommandParam> commandinputparam) {

		for (CommandParam commandparaminput : commandinputparam) {
			if (commandparaminput.getName().equals(TYPE_PARAMNAME)
					|| commandparaminput.getName().equals(PREFIX_PARAMNAME)
					|| commandparaminput.getName().equals(MSISDN_PARAMNAME)) {
				pcrfremoveParam.add(commandparaminput);
			} else if (commandparaminput.getName().equals(SERVICENAME_PARAM)) {
				CommandParamInput tempparaminput = new CommandParamInput(
						ParameterFeedType.INPUT, commandparaminput.getValue());
				tempparaminput.setName(SERVICENAMEREPLACE_PARAM);
				tempparaminput.setType(CommandParameterType.PRIMITIVE);
				tempparaminput.setValue(commandparaminput.getValue());
				pcrfremoveParam.add(tempparaminput);
			}
		}

	}
}
