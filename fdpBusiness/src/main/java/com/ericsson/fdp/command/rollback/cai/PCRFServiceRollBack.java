package com.ericsson.fdp.command.rollback.cai;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class will execute rollback method of ADD PCRF COmmand
 * @author ERICSSON
 *
 */
public class PCRFServiceRollBack extends NonTransactionCommand {

private static final long serialVersionUID = 1L;
    
   /* private static final List<String> PARAMETERS_TO_REPLACE = new LinkedList<>();
    private static final List<String> PARAMETERS_TO_BE_REPLACED = new LinkedList<>();*/
	private static final String PARAMETER_TO_REPLACE = "SERVICE,DEF,SERVNAME_COMM";
	private static final String PARAMETER_TO_BE_REPLACED = "SERVICE,DEL,SERVNAME_COMM";
    

    /*static {
        init();
    }
    public static void init() {
        PARAMETERS_TO_REPLACE.clear();
        PARAMETERS_TO_REPLACE.add("SERVICE,DEF,SERVNAME_COMM");
        PARAMETERS_TO_BE_REPLACED.clear();
        PARAMETERS_TO_BE_REPLACED.add("SERVICE,DEL,SERVNAME_COMM");

    }*/


    public PCRFServiceRollBack() {
        super();
    }


    public PCRFServiceRollBack(String commandDisplayName) {
        super(commandDisplayName);
    }


    @Override
    public Status execute(FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {

        final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
                new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.REMOVEPCRFSEVICE.getCommandDisplayName()));
        AbstractCommand removePCRF = (AbstractCommand) cachedCommand;

        FDPCommand executedCommand = fdpRequest.getExecutedCommand(Command.ADD_PCRF_SEVICE_V3.getCommandDisplayName());

        for (CommandParam param : executedCommand.getInputParam()) {
            CommandParamInput iParam = (CommandParamInput) param;
            /*if (PARAMETERS_TO_REPLACE.contains(iParam.getName())) {
                Object value = iParam.getValue();
                String paramName = PARAMETERS_TO_BE_REPLACED.get(PARAMETERS_TO_REPLACE.indexOf(iParam.getName()));
                PARAMETERS_TO_REPLACE.remove(iParam.getName());
                PARAMETERS_TO_BE_REPLACED.remove(paramName);
                CommandParam paramToBeReplaced = removePCRF.getInputParam(paramName);
                if (paramToBeReplaced != null) {
                    CommandParamInput iparamToBeReplaced = (CommandParamInput) paramToBeReplaced;
                    iparamToBeReplaced.setValue(value);
                    FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
                            iparamToBeReplaced, value);
                }
            }*/
            if (iParam.getName().equalsIgnoreCase(PARAMETER_TO_REPLACE)) {
            	 Object value = iParam.getValue();
            	 CommandParam paramToBeReplaced = removePCRF.getInputParam(PARAMETER_TO_BE_REPLACED);
            	 if (paramToBeReplaced != null) {
                     CommandParamInput iparamToBeReplaced = (CommandParamInput) paramToBeReplaced;
                     iparamToBeReplaced.setValue(value);
                     FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
                             iparamToBeReplaced, value);
                 }
            }
        }
        //init();
       return CommandUtil.executeCommand(fdpRequest, removePCRF, true);
    }

}
