package com.ericsson.fdp.business.adapter.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.direct.DirectConsumerNotAvailableException;
import org.apache.camel.component.direct.DirectEndpoint;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.bean.TelnetAdapterRequest;
import com.ericsson.fdp.business.cache.MetaDataCache;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.vo.Handset4GDetailVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.entity.FDPEMADetail;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.EMAInterfaceTypeEnum;
import com.ericsson.fdp.dao.enums.ExternalSystem;

public class EMATelnetAdapter<T> extends AbstractTelnetAdapter<T> {
	
	public EMATelnetAdapter(final TelnetAdapterRequest httpAdapterRequest, final ExternalSystem externalSystem,
			final T httpRequestToSet, final FDPRequest fdpRequest) {
		super(httpAdapterRequest.getEmaDetail().getUserName(), httpAdapterRequest.getEmaDetail().getPassword(),
				httpRequestToSet);
		this.telnetAdapterRequest = httpAdapterRequest;
		this.externalSystem = externalSystem;
		this.httpRequest = httpRequestToSet;
		this.fdpRequest = fdpRequest;
	}

	/**
	 * The fdp request, which is used to connect to appropriate external system
	 * for that circle.
	 */
	private final TelnetAdapterRequest telnetAdapterRequest;

	/**
	 * The external system to be used.
	 */
	private final ExternalSystem externalSystem;

	/**
	 * The request to be executed.
	 */
	private final T httpRequest;

	/** The context. */
	private CdiCamelContext context;

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTelnetAdapter.class);

	private final FDPRequest fdpRequest;
	
	@Override
	public Map<String, Object> callClient() throws ExecutionFailedException {
		final Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			LOGGER.debug("Invoking callClient()....  with adapter request as Telnet Request:" + telnetAdapterRequest);
			final String camelCircleEndpoint = BusinessConstants.CAMEL_DIRECT + telnetAdapterRequest.getEndPoint();
			context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			final Endpoint endpoint = context.getEndpoint(camelCircleEndpoint, DirectEndpoint.class);
			LOGGER.debug("Endpoint got from Request Adapter :" + camelCircleEndpoint);
			final String circleName = fdpRequest.getCircle().getCircleName();
			final String moduleName = BusinessModuleType.EMA_SOUTH.name();
			final String requestId = telnetAdapterRequest.getRequestId();
			final String circleCode = telnetAdapterRequest.getCircleCode();
			final Logger circleLoggerRequest = FDPLoggerFactory.getRequestLogger(circleName, moduleName);
			FDPLogger.info(circleLoggerRequest, getClass(), "process()",
					BusinessConstants.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId
							+ FDPConstant.LOGGER_DELIMITER + FDPConstant.EMACMDNM
							+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + telnetAdapterRequest.getCommandName()
							+ FDPConstant.LOGGER_DELIMITER + FDPConstant.EMACMD
							+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + telnetAdapterRequest.getLogValue());

			//final List<String> commandList = getCommandForAdapter();
			
			final Exchange exchange = endpoint.createExchange();
			exchange.setPattern(ExchangePattern.InOut);
			exchange.setProperty(BusinessConstants.CIRCLE_CODE, circleCode);
			LOGGER.debug("CircleCode got from Request Adapter :" + circleCode);
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, ExternalSystem.EMA);
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			exchange.setProperty(BusinessConstants.MSISDN, fdpRequest.getSubscriberNumber());
			//String command = appendCommand(commandList);
			//in.setBody(command);
			String responseOut = null;
			Long startTime = System.currentTimeMillis();
			
			if("EXTEND_PCRF_SERVICE".equals(telnetAdapterRequest.getCommandName()) || "ADD_PCRF_QUOTA".equals(telnetAdapterRequest.getCommandName())){
				FDPCommand command = isCommandExecuted(fdpRequest.getExecutedCommands(), "ADD_PCRF_SEVICE_V3");
				if(command != null && "0".equals(command.getOutputParam("Resp").getValue().toString())) {
					responseOut="RESP:0;";
					FDPLogger.info(circleLoggerRequest, getClass(), "callClient()",BusinessConstants.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + 
							requestId + FDPConstant.LOGGER_DELIMITER +"Command is not executed!" + responseOut);
				} else {
					exchange.getIn().setBody(getEmaCommand());
					final Producer producer = endpoint.createProducer();
					producer.process(exchange);
					responseOut = exchange.getOut().getBody(String.class);
					FDPLogger.info(circleLoggerRequest, getClass(), "callClient()",BusinessConstants.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + 
							requestId + FDPConstant.LOGGER_DELIMITER +  FDPConstant.EMACMDNM
							+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + telnetAdapterRequest.getCommandName() + 
							FDPConstant.LOGGER_DELIMITER +"Response: " + responseOut);
				}
			} else{
				exchange.getIn().setBody(getEmaCommand());
				final Producer producer = endpoint.createProducer();
				producer.process(exchange);
				responseOut = exchange.getOut().getBody(String.class);
				FDPLogger.info(circleLoggerRequest, getClass(), "callClient()",BusinessConstants.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + 
						requestId + FDPConstant.LOGGER_DELIMITER +  FDPConstant.EMACMDNM
						+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + telnetAdapterRequest.getCommandName() + 
						FDPConstant.LOGGER_DELIMITER +"Response: " + responseOut);
			}
							
			Long endTime = 	System.currentTimeMillis();	
			FDPLogger.debug(circleLoggerRequest, getClass(), "callClient()",
					BusinessConstants.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId
							+ FDPConstant.LOGGER_DELIMITER + FDPConstant.EMACMDNM
							+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + telnetAdapterRequest.getCommandName()
							+ FDPConstant.LOGGER_DELIMITER + "Execution Time(in millis)" 
							+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + (endTime - startTime));	
			
			if(responseOut!=null)
			{
				responseMap.put(BusinessConstants.RESPONSE_CODE, "200");
				responseMap.put(BusinessConstants.COMMAND_OUTPUT, responseOut);
			}
			else
			{
				responseMap.put(BusinessConstants.RESPONSE_CODE, "-200");
				responseMap.put(BusinessConstants.COMMAND_OUTPUT, "RESP:-200");
			}
			
			LOGGER.debug("Method terminated properly..");
		} catch (final DirectConsumerNotAvailableException consumer) {
			responseMap.put(BusinessConstants.RESPONSE_CODE, "-200");
			responseMap.put(BusinessConstants.COMMAND_OUTPUT, "RESP:-200");
		} catch (final Exception e) {
			LOGGER.debug("Exception :" + e.getMessage());
		}
		return responseMap;
	}

	@Override
	public String toString() {
		return " Telnet adapter , external system :- " + externalSystem.name() + " circle "
				+ telnetAdapterRequest.getCircleCode() + " request string :- " + httpRequest.toString();
	}

	public List<String> getCommandForAdapter() {
		if (telnetAdapterRequest == null) {
			throw new IllegalArgumentException("telnet Adpater Request found as null.");
		}
		final FDPEMADetail fdpemaDetail = telnetAdapterRequest.getEmaDetail();
		final List<String> commandList = new ArrayList<String>();
		final EMAInterfaceTypeEnum interfaceType = fdpemaDetail.getInterfaceType();
		final String userName = fdpemaDetail.getUserName();
		final String password = fdpemaDetail.getPassword();
		LOGGER.debug("Username: {} , password: {} got from DB  :", userName, password);
		LOGGER.debug("Interface Type Got:" + interfaceType);

		switch (interfaceType) {
		case MML:
			commandList.add(userName + BusinessConstants.EMA_COMMAND_END_DELIMITER);
			commandList.add(password + BusinessConstants.EMA_COMMAND_END_DELIMITER);
			commandList.add(httpRequest.toString() + BusinessConstants.EMA_COMMAND_END_DELIMITER);
			commandList.add("exit;" + BusinessConstants.EMA_COMMAND_END_DELIMITER);
			break;
		case CAI:
		//	commandList.add(BusinessConstants.EMA_COMMAND_END_DELIMITER);
			final String compiledLoginCmd = fdpemaDetail.getLogin() +":"+ userName + ":" + password + ";"
					+ BusinessConstants.EMA_COMMAND_END_DELIMITER;
			commandList.add(compiledLoginCmd);
			commandList.add(httpRequest.toString() + BusinessConstants.EMA_COMMAND_END_DELIMITER);
			commandList.add(fdpemaDetail.getLogout() + BusinessConstants.EMA_COMMAND_END_DELIMITER);
			break;
		default:
			throw new IllegalArgumentException("Illegal interface specified.");
		}
		LOGGER.debug("Commmand List: {} ", commandList);
		return commandList;
	}
	
	/**
	 * This method will return the EMA command
	 * @return
	 */
	public String getEmaCommand() {
		if (telnetAdapterRequest == null) {
			throw new IllegalArgumentException("telnet Adpater Request found as null.");
		}
		
		return httpRequest.toString() + BusinessConstants.EMA_COMMAND_END_DELIMITER;
	}
	
	private String appendCommand(List<String> commandList) {
		StringBuilder appender = new StringBuilder();
		
		for(String command: commandList) {
			appender.append(command);
		}
		
		return appender.toString();
	}

	/**
	 * 
	 * @param commandList
	 * @param name
	 * @return
	 */
	private boolean checkIfCommandExist(List<String> commandList , String name){
		for(String command : commandList){
			if(command.contains(name))
				return true;
		}
		return false;
	}
	
	/**
	 * Method to return provided command name form the fdpcommand list
	 * @param commandList
	 * @param commandName
	 * @return
	 */
	private FDPCommand isCommandExecuted(List<FDPCommand>commandList, String commandName) {
		FDPCommand pcrfCommand = null;;
		for (FDPCommand command : commandList) {
			if (command.getCommandDisplayName().equals(commandName)) {
				pcrfCommand = command;
				break;
			}
		}
		
		return pcrfCommand;
	}
 

}