package com.ericsson.fdp.business.route.processor.ivr.impl.commandservice;

import javax.xml.bind.JAXBException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.commandservice.IVRCommandEnum;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.impl.FDPWEBRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

/**
 * IVRCommandServiceProcessor class validates the command request, if request is
 * valid then executes the command and send response, otherwise send the invalid
 * command response.
 * 
 * @author Ericsson
 */
public class IVRCommandServiceProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(IVRCommandServiceProcessor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Message in = exchange.getIn();
		final String requestId = in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		final String commandName = in.getHeader(FulfillmentParameters.INPUT.getValue(), String.class);
		final String msisdn = getMsisdn(in);
		final FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		// final String circleCode =
		// in.getHeader(FulfillmentParameters.CIRCLE_CODE.getValue(),
		// String.class);
		/*final FDPCircle fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(msisdn,
				ApplicationConfigUtil.getApplicationConfigCache());*/
		LOGGER.debug("Checking for valid input parameter value.");
		final IVRCommandEnum ivrCommand = IVRCommandEnum.getIVRCommandEnum(commandName);

		if (ivrCommand == null) {
			String errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(requestId)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append("ERROR_CODE")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(String.format(FulfillmentResponseCodes.INVALID_PARAMETER.getDescription(), msisdn,
							fdpCircle.getCircleCode())).toString();
			FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(), "process()", errorDescription);
			sendResponse(exchange, FulfillmentResponseCodes.INVALID_PARAMETER, FulfillmentParameters.INPUT.getValue());
		} else {
			LOGGER.debug("Checking for valid input parameter DONE. value = {}.", ivrCommand.getIvrName());
			printPreLogsForReports(exchange, commandName);
			getCachedCommandAndExecute(exchange, ivrCommand);
		}
	}

	/**
	 * Prints the logs for reports.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param commandName
	 *            the command name
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void printPreLogsForReports(final Exchange exchange, final String commandName)
			throws ExecutionFailedException {
		final Message in = exchange.getIn();
		logInfoMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "INIP:{}|LNAME:{}|CH:{}|MSISDN:{}",
				in.getHeader(FDPRouteHeaders.INCOMING_IP.getValue()),
				in.getHeader(FDPRouteHeaders.CHANNEL_NAME.getValue()),
				in.getHeader(FDPRouteHeaders.CHANNEL_NAME.getValue()), in.getHeader(FDPRouteHeaders.MSISDN.getValue()));
		logInfoMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "SID:{}",
				in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue()));
		logInfoMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "TBH:CommandService");
		logInfoMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "ACTN:{}| CHCODE:{}/{}|TP:Public-Service",
				commandName, in.getHeader("CamelHttpUrl"), in.getHeader("CamelHttpQuery"));
	}

	/**
	 * Gets the cached command and execute.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param ivrCommand
	 *            the ivr command
	 * @return the cached command and execute
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws JAXBException 
	 */
	private void getCachedCommandAndExecute(final Exchange exchange, final IVRCommandEnum ivrCommand)
			throws ExecutionFailedException, JAXBException {
		final Message in = exchange.getIn();
		final FDPCircle fdpCircle = in.getHeader(FDPConstant.FDP_CIRCLE, FDPCircle.class);
		if (fdpCircle == null) {
			throw new ExecutionFailedException("fdpCircle not found in exchange.");
		} else {
			final String fdpCommandName = ivrCommand.getCommand().getCommandDisplayName();
			logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "Getting command : {} from cache.",
					fdpCommandName);
			final FDPCacheable cachedCommand = ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpCircle, ModuleType.COMMAND, fdpCommandName));
			if (cachedCommand != null && cachedCommand instanceof FDPCommand) {
				logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "Command : {} found in cache.",
						fdpCommandName);
				final String msisdn = in.getHeader(FulfillmentParameters.MSISDN.getValue(), String.class);
				final FDPCommand command = (FDPCommand) cachedCommand;
				final FDPCommand fdpCommand = CommandUtil.getExectuableFDPCommand(command);
				if(null == fdpCommand) {
					throw new ExecutionFailedException("Unable to create " + fdpCommandName +" command.");
				}
				executeCommandAndSendResponse(exchange, fdpCircle, fdpCommand, msisdn);
			} else {
				throw new ExecutionFailedException("Command " + fdpCommandName + " not found in cache.");
			}
		}
	}

	/**
	 * Execute command and send response.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param fdpCircle
	 *            the fdp circle
	 * @param cachedCommand
	 *            the cached command
	 * @param msisdn
	 *            the msisdn
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws JAXBException 
	 */
	private void executeCommandAndSendResponse(final Exchange exchange, final FDPCircle fdpCircle,
			final FDPCacheable cachedCommand, final String msisdn) throws ExecutionFailedException, JAXBException {
		final FDPCommand fdpCommand = (FDPCommand) cachedCommand;
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "Creating Command request.");
		final String requestId = exchange.getIn().getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		final String iName = exchange.getIn().getHeader(FulfillmentParameters.INVOCATOR_NAME.getValue(), String.class);
		FDPWEBRequestImpl request = null;
		if("WEB".equalsIgnoreCase(iName)){
			request = RequestUtil.getIVRandWebRequest(msisdn,generateTransactionId(), fdpCircle,requestId ,ChannelType.WEB);
		}else{
			request = RequestUtil.getIVRandWebRequest(msisdn,generateTransactionId(), fdpCircle,requestId ,ChannelType.IVR);
		}		
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "Request created.Executing Command.");
		final Status status = fdpCommand.execute(request);
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "Command Execution Status : {}.", status);
		printPostRequestLogs(exchange);
		logInfoMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "INRESULT:"+Status.SUCCESS.getStatusText());
		if (Status.SUCCESS.equals(status)) {
			sendCommandSuccessResponse(exchange, fdpCommand);
		} else {
			setErrorResponse(exchange, fdpCommand.getResponseError());
		}
	}

	/**
	 * Send command success response.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param fdpCommand
	 *            the fdp command
	 * @throws ExecutionFailedException
	 * @throws JAXBException 
	 */
	private void sendCommandSuccessResponse(final Exchange exchange, final FDPCommand fdpCommand)
			throws ExecutionFailedException, JAXBException {
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "Creating command success response.");
		String responseValue = null;
		if (fdpCommand instanceof AbstractCommand) {
			responseValue = ((AbstractCommand) fdpCommand).getCommandResponse();
		}
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS,
				"sending command success response.SystemType : {}, responseCode : {}, responseValue : {}", fdpCommand
						.getSystem().name(), FulfillmentResponseCodes.SUCCESS.getResponseCode(), responseValue);
		sendResponse(exchange, FulfillmentResponseCodes.SUCCESS, null, null, responseValue, "");
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS,
				"Command success response SENT.SystemType : {}, responseCode : {}, responseValue : {}",
				FulfillmentResponseCodes.SUCCESS.getSystemType(), FulfillmentResponseCodes.SUCCESS.getResponseCode(), responseValue);
	}

}
