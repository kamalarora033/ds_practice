package com.ericsson.fdp.business.serviceprovisioning.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.SPCacheUtil;
import com.ericsson.fdp.business.display.impl.ResponseMessageImpl;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

/**
 * This class implements the service provisioning.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class ServiceProvisioningImpl implements ServiceProvisioning {

	/**
	 *
	 */
	private static final long serialVersionUID = 5950584405356776554L;

	//
	// @Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	// private FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache;

	@Override
	public FDPResponse executeServiceProvisioning(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		// Generate Transaction Id.
		final ServiceProvisioningRule fdpServiceProvisioningRule = getStepsForServiceProvisioning(fdpRequest);
		FDPLogger.debug(circleLogger, getClass(), "executeServiceProvisioning()",
				LoggerUtil.getRequestAppender(fdpRequest) + "Executing service provisioning.");
		FDPResponse fdpResponse = null;
		if (fdpServiceProvisioningRule != null) {
			try {
				fdpResponse = fdpServiceProvisioningRule.execute(fdpRequest);
			} catch (final RuleException e) {
				FDPLogger.error(circleLogger, getClass(), "executeServiceProvisioning()",
						LoggerUtil.getRequestAppender(fdpRequest)
								+ "The service provisioning could not be done due to an error.", e);
				throw new ExecutionFailedException("The service provisioning could not be done due to an error.", e);
			}
		} else {
			FDPLogger.error(circleLogger, getClass(), "executeServiceProvisioning()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "The service provisioning id could not be found in the cache.");
			throw new ExecutionFailedException("The service provisioning id could not be found in the cache.");
		}
		final Status responseStatus = fdpResponse == null ? Status.FAILURE : fdpResponse.getExecutionStatus();
		final ResponseError responseError = getResponseError(fdpResponse);
		final StringBuilder stringBuilder = new StringBuilder();
		
		 if( responseStatus!=null &&  responseStatus.equals(Status.FAILURE) &&
				fdpRequest.getExecutedCommand(Command.GET_ACCUMULATORS.getCommandDisplayName())!=null)
			{
				getUAOverrideNotification(fdpRequest,fdpResponse,stringBuilder, responseStatus);
					
			}
		 if (stringBuilder.length() == 0 && responseStatus!=null && Status.FAILURE.equals(responseStatus)) {
			 
			 stringBuilder.append(LoggerUtil.getRequestAppender(fdpRequest)).append("PROVRSLT")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseStatus);
			 
			stringBuilder.append(FDPConstant.LOGGER_DELIMITER).append("PROVRSN")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("responseCode=")
					.append((!StringUtil.isNullOrEmpty(responseError.getResponseCode()) ? responseError.getResponseCode() : FDPConstant.UNKOWN_RESPONSE_CODE)).append(";Desc=")
					.append((!StringUtil.isNullOrEmpty(responseError.getResponseErrorString()) ? responseError.getResponseErrorString() : FDPConstant.UNKOWN_RESPONSE_DESC))
					.append(";External Node=").append((!StringUtil.isNullOrEmpty(responseError.getSystemType()) ? responseError.getSystemType() : FDPConstant.UNKOWN_SYSTEM));
		}
		if (stringBuilder.length() == 0) {
			stringBuilder.append(LoggerUtil.getRequestAppender(fdpRequest)).append("PROVRSLT")
			.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseStatus);
		}

		getUALog(fdpRequest, circleLogger, stringBuilder);
		stringBuilder.append(FDPConstant.LOGGER_DELIMITER).append(FDPLoggerConstants.CORRELATION_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
		.append(fdpRequest.getOriginTransactionID().toString());
		FDPLogger.info(circleLogger, getClass(), "executeServiceProvisioning()", stringBuilder.toString());

		return fdpResponse;
	}

	private ResponseError getResponseError(final FDPResponse fdpResponse) {
		return (fdpResponse == null || (null == fdpResponse.getResponseError())) ? new ResponseError("NA", "Unknown Error occurred", ErrorTypes.UNKNOWN.name(),FDPConstant.EXTERNAL_SYSTEM_FDP)
				: fdpResponse.getResponseError();
	}

	/**
	 * This method is used to get the steps for the service provisioning.
	 * 
	 * @param fdpRequest
	 *            The service provisioning input.
	 * @return The rule for the service provisioning.
	 * @throws ExecutionFailedException 
	 */
	private ServiceProvisioningRule getStepsForServiceProvisioning(final FDPRequest fdpRequest) throws ExecutionFailedException {
		ServiceProvisioningRule serviceProvisioningRule = null;
		final FDPCacheable serviceProvisioningObject = fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
		if (serviceProvisioningObject instanceof ServiceProvisioningRule) {
			serviceProvisioningRule = (ServiceProvisioningRule) serviceProvisioningObject;
			serviceProvisioningRule = SPCacheUtil.getExecutableFDPSP(serviceProvisioningRule);
			RequestUtil.updateProductInRequest(fdpRequest,(FDPCacheable) serviceProvisioningRule,null);
		}
		return serviceProvisioningRule;
		/*
		 * List<FDPStep> fdpStepsToSet = new ArrayList<FDPStep>(); FDPStep
		 * fdpStep = new FDPCommandStep(getCommand());
		 * fdpStepsToSet.add(fdpStep); FDPServiceProvisioningNotificationImpl
		 * fdpServiceProvisioningNotificationImpl = new
		 * FDPServiceProvisioningNotificationImpl( getNotification());
		 * FDPServiceProvisioningRule fdpServiceProvisioningRule = new
		 * FDPServiceProvisioningRule(fdpStepsToSet,
		 * fdpServiceProvisioningNotificationImpl); return
		 * fdpServiceProvisioningRule;
		 * 
		 * FDPMetaBag fdpMetaBag = new FDPMetaBag();
		 * fdpMetaBag.setFdpCircle((FDPCircle)
		 * fdpServiceProvisioningInput.getValueFromRequest("circle"));
		 * fdpMetaBag.setModuleType(ModuleType.SP);
		 * fdpMetaBag.setModuleId(fdpServiceProvisioningInput
		 * .getServiceProvisioningId()); FDPCacheable fdpCacheable =
		 * fdpMetaDataCache.getValue(fdpMetaBag); if (fdpCacheable instanceof
		 * FDPServiceProvisioningRule) { FDPServiceProvisioningRule
		 * serviceProvisioningRule = (FDPServiceProvisioningRule) fdpCacheable;
		 * return serviceProvisioningRule; } return null;
		 */
	}

	/*
	 * private Map<Status, FDPNotificationDTO> getNotification() { Map<Status,
	 * FDPNotificationDTO> nMap = new HashMap<Status, FDPNotificationDTO>();
	 * return nMap; }
	 * 
	 * private FDPCommand getCommand() { FDPNonTransactionCommand fdpCommand =
	 * new FDPNonTransactionCommand();
	 * fdpCommand.setCommandDisplayName("GetAccountDetails");
	 * fdpCommand.setCommandName("getAccountDetails");
	 * fdpCommand.setCommandExecutionType(CommandExecutionType.UCIP);
	 * fdpCommand.setInputParam(getInputParam(fdpCommand)); return fdpCommand; }
	 * 
	 * private List<FDPCommandParam> getInputParam(FDPCommand fdpCommand) {
	 * List<FDPCommandParam> commandParams = new ArrayList<FDPCommandParam>();
	 * FDPCommandParamInput commandParam = new
	 * FDPCommandParamInput(CommandParameterSource.INPUT, "345");
	 * commandParam.setName("originId"); commandParam.setCommand(fdpCommand);
	 * commandParam.setPrimitiveValue(Primitives.STRING);
	 * commandParams.add(commandParam);
	 * commandParam.setType(CommandParameterType.Primitive); return
	 * commandParams; }
	 */

	/**
	 * Fetches UAID from circle configurations and appends its value to logger, if GetAccumulator is attached in sp.
	 * @param fdpRequest
	 * @param circleLogger
	 */
	private static void getUALog(final FDPRequest fdpRequest, Logger circleLogger, StringBuilder uaResponse) {
		if(LoggerUtil.isUALogEnabled(fdpRequest))
		{
			try {
				if(null != fdpRequest.getExecutedCommand(Command.GET_ACCUMULATORS.getCommandDisplayName())){
					
					String[] uaIDs = (fdpRequest.getCircle().getConfigurationKeyValueMap().get(FDPConstant.UA_LOGGING_ID)).split(",");
					for(int index = 0; index < (uaIDs.length); index++) {
						String ua = FDPConstant.UA + FDPConstant.UNDERSCORE + uaIDs[index].trim() + FDPConstant.UNDERSCORE + "VALUE";
						
						uaResponse.append(FDPConstant.LOGGER_DELIMITER).append("UA").append(uaIDs[index].trim()).append(FDPConstant.EQUAL);
						uaResponse.append(MVELUtil.evaluateMvelExpression(fdpRequest, ua, Primitives.LONG)).append(FDPConstant.SPACE);
					}					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			//FDPLogger.info(circleLogger, ServiceProvisioningRule.class, "getUALog()", uaResponse.toString());
		}
	}
	
	
	private  void getUAOverrideNotification(final FDPRequest fdpRequest,FDPResponse fdpResponse,StringBuilder stringBuilder, Status responseStatus) {
				try {
					final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
					String recurringType = null;
					
					if (product instanceof Product) {
						recurringType = ((Product) product).getAdditionalInfo(ProductAdditionalInfoEnum.RECURRING_TYPE);
					}
				if(null != recurringType && recurringType.equals("2")){
					stringBuilder.append(LoggerUtil.getRequestAppender(fdpRequest)).append("PROVRSLT")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(responseStatus);
					
					String[] uaIDs = (fdpRequest.getCircle().getConfigurationKeyValueMap().get(FDPConstant.UA_NOTIFICATION_ID)).split("\\|");
					for(int index = 0; index < (uaIDs.length); index++) {
						String priorityArray[] = uaIDs[index].trim().split(":");
						if(priorityArray[1].trim().equalsIgnoreCase(FDPConstant.PRIORITY_ONE)){
						String ua = FDPConstant.UA + FDPConstant.UNDERSCORE + priorityArray[0].trim() + FDPConstant.UNDERSCORE + "VALUE";
						String uaValue = String.valueOf(MVELUtil.evaluateMvelExpression(fdpRequest, ua, Primitives.LONG));
						getUANotificationMap(fdpRequest,priorityArray[0].trim(),uaValue,fdpResponse,stringBuilder);
						}
					}					
				}
			} catch (Exception e) {
				e.printStackTrace();
				
		}
			
	}
	
	/**
	 * This method override values of accumulator id and display messages respectively
	 * @return
	 */
	private void getUANotificationMap(FDPRequest fdpRequest,String uaId,String uaValue,FDPResponse fdpResponse,StringBuilder stringBuilder){
		//HashMap<String, String> uaIdMap = new HashMap<String, String>();
		String notificationText = null;
		 final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		String uaNotification = configurationMap.get(ConfigurationKey.UA_BASED_NOTIFICATION.getAttributeName());
		String notifications[] = uaNotification.trim().split("\\|"); 
		for(int i = 0; i < notifications.length; i++){
			String account[] = notifications[i].trim().split(":");
			if(null!=account[0].trim() && account[0].equalsIgnoreCase(uaId)
					&& account[1].trim().contains(uaValue))
				{
				notificationText = notifications[i];
			}
		}
		
		if(notificationText!=null && !notificationText.isEmpty()){
			String textArray[] = notificationText.split(":");
			List<ResponseMessage> responseString = fdpResponse
					.getResponseString();
			/*Iterator it = responseString.iterator();
			if (it.hasNext()) {
				ResponseMessageImpl message = (ResponseMessageImpl) it.next();
				message.setCurrDisplayText(textArray[2], DisplayArea.COMPLETE);
			}*/
			fdpResponse.getResponseError().setResponseErrorString(textArray[2]);
			fdpResponse.getResponseError().setResponseCode(textArray[0]+FDPConstant.ZERO+textArray[1]);
			stringBuilder.append(FDPConstant.LOGGER_DELIMITER).append("PROVRSN")
			.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("responseCode=")
			.append((!StringUtil.isNullOrEmpty(fdpResponse.getResponseError().getResponseCode()) ? fdpResponse.getResponseError().getResponseCode() : FDPConstant.UNKOWN_RESPONSE_CODE)).append(";Desc=")
			.append((!StringUtil.isNullOrEmpty(fdpResponse.getResponseError().getResponseErrorString()) ?fdpResponse.getResponseError().getResponseErrorString() : FDPConstant.UNKOWN_RESPONSE_DESC))
			.append(";External Node=").append((!StringUtil.isNullOrEmpty(fdpResponse.getResponseError().getSystemType()) ? fdpResponse.getResponseError().getSystemType() : FDPConstant.UNKOWN_SYSTEM));
		}
				
	}
}
