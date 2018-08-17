package com.ericsson.fdp.business.step.execution.impl.tariffEnquiry;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.constants.FDPStepResponseConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.business.step.execution.FDPExecutionService;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

@Stateless
public class CheckBalanceServiceImpl  implements FDPExecutionService{

	@Override
	public FDPStepResponse executeService(FDPRequest fdpRequest,
			Object... additionalInformations) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "executeService()",
				"Executing Service for requestId:" + fdpRequest.getRequestId());
		return getResponseForRequestedAttribute(fdpRequest, circleLogger);	
	}

	/**
	 * This method will execute the Check Balance Params population in map.
	 * 
	 * @param requestedAttributes
	 * @param fdpRequest
	 * @param circleLogger
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private FDPStepResponse getResponseForRequestedAttribute(final FDPRequest fdpRequest,
			 final Logger circleLogger) throws ExecutionFailedException {
		//Execute the Commands.
		final String commandGBAD="GetBalanceAndDate";
		final String commandGetThreshold = "GetUsageThresholdsAndCounters";
		executeCachedComamnd(fdpRequest, commandGBAD);
		executeCachedComamnd(fdpRequest, commandGetThreshold);
		
		//Parse Response.
		Map<String, String> responseMap = evaluateDADetailsForUser(fdpRequest.getExecutedCommand(commandGBAD));
		responseMap.putAll(evaluateUCUTDetailsForUser(fdpRequest.getExecutedCommand(commandGetThreshold)));
		//System.out.println("MVEL_INPUT map:"+responseMap);
		//Populate AUX map.
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.MVEL_INPUT, responseMap);
		
		FDPStepResponseImpl stepResponseImpl = new FDPStepResponseImpl();
		stepResponseImpl.addStepResponseValue(FDPStepResponseConstants.STATUS_KEY, true);
		return stepResponseImpl;
	}
	
	/**
	 * This method will execute the commands.
	 * 
	 * @param fdpRequest
	 * @param commandName
	 * @return
	 * @throws ExecutionFailedException
	 */
	private void executeCachedComamnd(final FDPRequest fdpRequest, final String commandName)  throws ExecutionFailedException{
		FDPCommand fdpCommand = null;
		final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandName));
		if(null != fdpCommandCached && fdpCommandCached instanceof AbstractCommand) {
			fdpCommand = (AbstractCommand) fdpCommandCached;
			if (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
				fdpRequest.addExecutedCommand(fdpCommand);
			} else {
				throw new ExecutionFailedException("Failed to execute command:"+commandName);
			}
		} else {
			throw new ExecutionFailedException("Command not found in cache"+ commandName);
		}
	}
	
	/**
	 * This method will parse the DA related details.
	 * 
	 * @param executedCommand
	 * @return
	 */
	private static Map<String,String> evaluateDADetailsForUser(final FDPCommand executedCommand) {
        final Map<String,String> responseMap = new HashMap<String, String>();
        String pathkey = null;
        int i = 0;
        final String paramterName = "dedicatedAccountInformation";
        final String dedicatedAccountId  = "dedicatedAccountId";
        final String dedicatedAccountValue1 = "dedicatedAccountValue1";
        final String expiryDate = "expiryDate";
        
        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +dedicatedAccountId)) != null) {
            final String dedicatedAccountValue1_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + dedicatedAccountValue1;
            final String expiryDate_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + expiryDate;
            
            final String userDaId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final String userDaValue = executedCommand.getOutputParam(dedicatedAccountValue1_Value).getValue().toString();
            final String userDaExpiry = executedCommand.getOutputParam(expiryDate_Value).getValue().toString();
            responseMap.put(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+userDaId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userDaValue);
            responseMap.put(FDPCSAttributeValue.DA.name()+FDPConstant.UNDERSCORE+userDaId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.EXPIRY.name(), userDaExpiry);
            i++;
        }
        return responseMap;
    }
	
	/**
	 * This method will populate the UC and UT related values from response.
	 * @param executedCommand
	 * @return
	 */
	private static Map<String,String> evaluateUCUTDetailsForUser(final FDPCommand executedCommand) {
        final Map<String,String> responseMap = new HashMap<String, String>();
        String pathkey = null;
        int i = 0;
        final String paramterName = "usageCounterUsageThresholdInformation";
        final String usageCounterID  = "usageCounterID";
        final String usageCounterValue = "usageCounterValue";
        final String usageThresholdInformation = "usageThresholdInformation";
        final String usageThresholdID  = "usageThresholdID";
    	final String usageThresholdValue = "usageThresholdValue";
        
        while (executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                + i + FDPConstant.PARAMETER_SEPARATOR +usageCounterID)) != null) {
            final String usageCounterValue_Value = paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + usageCounterValue;
            final String userUCId = executedCommand.getOutputParam(pathkey).getValue().toString();
            final String userUCValue = executedCommand.getOutputParam(usageCounterValue_Value).getValue().toString();
            responseMap.put(FDPCSAttributeValue.UC.name()+FDPConstant.UNDERSCORE+userUCId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userUCValue);
            int j=0;
            while(executedCommand.getOutputParam(pathkey = (paramterName + FDPConstant.PARAMETER_SEPARATOR
                    + i + FDPConstant.PARAMETER_SEPARATOR + usageThresholdInformation +FDPConstant.PARAMETER_SEPARATOR + j + FDPConstant.PARAMETER_SEPARATOR + usageThresholdID)) != null) {
            	final String userUTId = executedCommand.getOutputParam(pathkey).getValue().toString();
                final String userUTValue = paramterName + FDPConstant.PARAMETER_SEPARATOR
                        + i + FDPConstant.PARAMETER_SEPARATOR + usageThresholdInformation +FDPConstant.PARAMETER_SEPARATOR + j + FDPConstant.PARAMETER_SEPARATOR + usageThresholdValue;
                responseMap.put(FDPCSAttributeValue.UT.name()+FDPConstant.UNDERSCORE+userUTId+FDPConstant.UNDERSCORE+FDPCSAttributeParam.VALUE.name(), userUTValue);
                j++;
            }
            i++;
        }
        return responseMap;
    }

	@Override
	public FDPStepResponse performRollback(FDPRequest fdpRequest,
			Map<ServiceStepOptions, String> additionalInformation) {
		// TODO Auto-generated method stub
		return null;
	}

}
