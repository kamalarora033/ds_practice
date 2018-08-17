package com.ericsson.fdp.command.rollback.cai;

import java.util.LinkedList;
import java.util.List;

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

public class AddBBBundleRollBack extends NonTransactionCommand {

    private static final long serialVersionUID = 1L;
    
    private static final List<String> PARAMETERS_TO_REPLACE = new LinkedList<>();
    private static final List<String> PARAMETERS_TO_BE_REPLACED = new LinkedList<>();
    

    static {
        init();
    }
    public static void init() {
        PARAMETERS_TO_REPLACE.clear();
        PARAMETERS_TO_REPLACE.add("GPRS,DEF,PDPCONTEXT,APNID_COMM");
        PARAMETERS_TO_REPLACE.add("DEF,PDPCONTEXT,APNID_COMM");
        PARAMETERS_TO_REPLACE.add("DEF,PDPCONTEXT,APNID_COMM");
        PARAMETERS_TO_BE_REPLACED.clear();
        PARAMETERS_TO_BE_REPLACED.add("GPRS,DEL,PDPCONTEXT,APNID_COMM");
        PARAMETERS_TO_BE_REPLACED.add("DEL,PDPCONTEXT,APNID_COMM");
        PARAMETERS_TO_BE_REPLACED.add("DEL,PDPCONTEXT,APNID_COL");
    }


    public AddBBBundleRollBack() {
        super();
    }


    public AddBBBundleRollBack(String commandDisplayName) {
        super(commandDisplayName);
    }


    @Override
    public Status execute(FDPRequest fdpRequest, Object... otherParams) throws ExecutionFailedException {

        final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
                new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, Command.REMOVE_BB_BUNDLE.getCommandDisplayName()));
        AbstractCommand removeBlackBerry = (AbstractCommand) cachedCommand;
        
        FDPCommand executedCommand = fdpRequest.getExecutedCommand(Command.ADD_BB_BUNDLE.getCommandDisplayName());

        for (CommandParam param : executedCommand.getInputParam()) {
            CommandParamInput iParam = (CommandParamInput) param;
            if (PARAMETERS_TO_REPLACE.contains(iParam.getName())) {
                Object value = iParam.getValue();
                String paramName = PARAMETERS_TO_BE_REPLACED.get(PARAMETERS_TO_REPLACE.indexOf(iParam.getName()));
                PARAMETERS_TO_REPLACE.remove(iParam.getName());
                PARAMETERS_TO_BE_REPLACED.remove(paramName);
                CommandParam paramToBeReplaced = removeBlackBerry.getInputParam(paramName);
                if (paramToBeReplaced != null) {
                    CommandParamInput iparamToBeReplaced = (CommandParamInput) paramToBeReplaced;
                    iparamToBeReplaced.setValue(value);
                    FulfillmentUtil.updateValueInInputCmdObject(CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
                            iparamToBeReplaced, value);
                }
            }
        }
        init();
       removeBlackBerry.setRollbackCommand(true);
       return CommandUtil.executeCommand(fdpRequest, removeBlackBerry, true);
    }
}
