package com.ericsson.fdp.business.fulfillment.service.impl;

import java.util.List;

import javax.ejb.Stateless;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.request.impl.FDPMetadataResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.CommandParameterType;

@Stateless
public class AbilitySyncUpFulfillmentServiceImpl extends AbstractFDPFulFillmentServiceImpl {
	
	Logger logger = null;
	
    protected /* varargs */ FDPResponse executeService(FDPRequest fdpRequest, Object ... additionalInformations) throws ExecutionFailedException {
		logger = getCircleLogger(fdpRequest);
		FDPLogger.debug(logger, getClass(), "execute AbilitySyncUpFulfillmentServiceImpl", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Start executeService Called:" + fdpRequest.getRequestId());
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		
		final FDPCacheable cachedCommand = ApplicationConfigUtil
				.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(),
								ModuleType.COMMAND,
								Command.ABILITY_SYNC_UP
										.getCommandDisplayName()));
		FDPCommand abiltySyncUpCommand = (FDPCommand) cachedCommand;
		
		List<CommandParam> commandParamList = abiltySyncUpCommand.getInputParam();
		
		for(int i=0; i<commandParamList.size(); i++) {
			CommandParam commandParam = commandParamList.get(i);
			String commandParamName = commandParam.getName();
			
			if(commandParamName.equalsIgnoreCase(FulfillmentParameters.EXTERNAL_REFERENCE.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.EXTERNAL_REFERENCE);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.EXTERNAL_APPLICATION.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.EXTERNAL_APPLICATION);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.EXTERNAL_USER.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.EXTERNAL_USER);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.REQUESTED_APPLICATION.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.REQUESTED_APPLICATION);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.OPERATION_NAME.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.OPERATION_NAME);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.ENTITY_ID.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.ENTITY_ID);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.OFFER_CODE.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.OFFER_CODE);			
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.SUBSCRIPTION_FLAG.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.SUBSCRIPTION_FLAG);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.ACTIVATION_DATE.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.ACTIVATION_DATE);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.EXPIRY_DATE_ABILITY.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.EXPIRY_DATE_ABILITY);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.OFFER_CHARGE.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.OFFER_CHARGE);
			} else if(commandParamName.equalsIgnoreCase(FulfillmentParameters.ORDER_STATUS.getValue())){
				commandParamList = updaetCommandParam(fulfillmentRequestImpl, commandParamList, i, FulfillmentParameters.ORDER_STATUS);
			}

		}
		
		
		if (abiltySyncUpCommand instanceof FDPCommand) {
			CommandUtil.executeCommandAbility(fdpRequest, (FDPCommand) abiltySyncUpCommand, false);
			
		} else {
			throw new ExecutionFailedException("AbilitySyncUp Command failed");
		}
		
		FDPMetadataResponseImpl fdpResponse = new FDPMetadataResponseImpl(Status.SUCCESS, true, null);
		
		
		return fdpResponse;
	
	}

    
    private List<CommandParam> updaetCommandParam(FulfillmentRequestImpl fulfillmentRequestImpl, List<CommandParam> commandParamList, int i, FulfillmentParameters fulfillmentParam) {
    	String commandInputValue = fulfillmentRequestImpl.getCommandInputParams(fulfillmentParam);
		
		final CommandParamInput commandParamIn = new CommandParamInput(ParameterFeedType.INPUT, commandInputValue);
		commandParamIn.setName(fulfillmentParam.getValue());
		commandParamIn.setPrimitiveValue(Primitives.STRING);
		commandParamIn.setType(CommandParameterType.PARAM_IDENTIFIER);
		commandParamIn.setValue(commandInputValue);
		
		commandParamList.set(i, commandParamIn);
		
		return commandParamList;
		
	}


}
 