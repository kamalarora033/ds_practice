package com.ericsson.fdp.business.airrecharge.service.impl;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.airrecharge.AirRecharge;
import com.ericsson.fdp.business.airrecharge.service.AirRechargeProcessor;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.OperatingMode;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.AIRCommandUtil;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.CommandExecutionType;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This class provides the implementation for the air recharge.
 * 
 * @author Ericsson
 * 
 */
@Stateless(name = "AirRechargeProcessorImpl")
public class AirRechargeProcessorImpl implements AirRechargeProcessor {

	/**
	 * The air recharge interface.
	 */
	@Inject
	private AirRecharge airRecharge;

	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	@Override
	public String executeAirRecharge(final String inputXML, final String requestId, final OperatingMode operatingMode,
			final String incomingIpAddress) throws ExecutionFailedException {

		Status executionStatus = Status.FAILURE;
		Logger circleLogger = FDPLoggerFactory.getRequestLogger(FDPLoggerFactory.DEFAULT_CIRCLE,
				BusinessModuleType.AIR_SOUTH.name());
		final FDPRequestImpl fdpRequestImpl = new FDPRequestImpl();
		try {
			final Map<String, CommandParam> xmlAsParams = CommandUtil.fromXmlToParameters(inputXML,
					CommandExecutionType.AIR);
			final CommandParam subscriberNumberParam = xmlAsParams.get(FDPConstant.AIR_SUBSCRIBER_NUMBER_PATH);
			if (subscriberNumberParam == null) {
				FDPLogger.info(circleLogger, getClass(), "executeAirRecharge()", LoggerUtil.getRequestAppender("")
						+ FDPConstant.LOGGER_DELIMITER + "MSISDN" + FDPConstant.LOGGER_DELIMITER + "Undefined");
				logHttpParameters(circleLogger, fdpRequestImpl, xmlAsParams);
			} else {
				final Long subscriberNumber = Long.parseLong(subscriberNumberParam.getValue().toString());
				final FDPCircle fdpCircle = RequestUtil.getFDPCircleFromMsisdn(subscriberNumber.toString());
				if (fdpCircle != null) {
					circleLogger = FDPLoggerFactory.getRequestLogger(fdpCircle.getCircleName(),
							BusinessModuleType.AIR_SOUTH.name());
					fdpRequestImpl.setChannel(ChannelType.AIR_RECHARGE);
					fdpRequestImpl.setCircle(fdpCircle);
					final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(
							subscriberNumber.toString(), fdpCircle);
					fdpRequestImpl.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
					fdpRequestImpl.setOriginHostName(circleConfigParamDTO.getOriginHostName());
					fdpRequestImpl.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
					fdpRequestImpl.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
					fdpRequestImpl.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
					final Long transactionId = generateTransactionId();
					fdpRequestImpl.setRequestId("AirRecharge_" + transactionId + "_"
							+ ThreadLocalRandom.current().nextLong());
					fdpRequestImpl.setOriginTransactionID(transactionId);
					FDPLogger.info(circleLogger, getClass(), "executeAirRecharge()",
							LoggerUtil.getRequestAppender(fdpRequestImpl) + FDPConstant.INCOMING_IP
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + incomingIpAddress
									+ FDPConstant.LOGGER_DELIMITER + FDPConstant.CHANNEL_TYPE
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + FDPConstant.AIRRECHARGE_CHANNELNAME);
					FDPLogger.info(circleLogger, getClass(), "executeAirRecharge()",
							LoggerUtil.getRequestAppender(fdpRequestImpl) + "CGWMODE"
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + operatingMode.name());
					logHttpParameters(circleLogger, fdpRequestImpl, xmlAsParams);
					FDPLogger.info(circleLogger, getClass(), "executeAirRecharge()",
							LoggerUtil.getRequestAppender(fdpRequestImpl.getRequestId()) + "MOB"
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + subscriberNumber);
					fdpRequestImpl.addExecutedCommand(getCommand(inputXML, xmlAsParams));
					executionStatus = airRecharge.executeAirRecharge(fdpRequestImpl);
				} else {
					FDPLogger
							.info(circleLogger, getClass(), "executeAirRecharge()", LoggerUtil.getRequestAppender("")
									+ FDPConstant.LOGGER_DELIMITER + "MSISDN" + FDPConstant.LOGGER_DELIMITER
									+ subscriberNumber);
					FDPLogger.debug(circleLogger, getClass(), "executeAirRecharge()",
							"Circle was not found for msisdn " + subscriberNumber);
					logHttpParameters(circleLogger, fdpRequestImpl, xmlAsParams);
				}

			}
		} catch (final NamingException e) {
			FDPLogger.error(circleLogger, getClass(), "executeAirRecharge()",
					"The air recharge could not be completed.", e);
			throw new ExecutionFailedException("The air recharge could not be completed.", e);
		} catch (final EvaluationFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "executeAirRecharge()",
					"The air recharge could not be completed.", e);
			throw new ExecutionFailedException("The air recharge could not be completed.", e);
		}
		final String response = AIRCommandUtil
				.createResponseXML(getCommandParamterFromExecutionStatus(executionStatus));
		final StringBuilder logginfo = new StringBuilder();
		logginfo.append(LoggerUtil.getRequestAppender(fdpRequestImpl.getRequestId())).append("CWGACTN")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		if (Status.SUCCESS.equals(executionStatus)) {
			logginfo.append("Served");
		} else {
			logginfo.append("Discarded");
		}
		FDPLogger.info(circleLogger, getClass(), "executeAirRecharge()", logginfo.toString());
		return response;
	}

	/**
	 * This method is used to log the http parameters.
	 * 
	 * @param circleLogger
	 *            The circle logger.
	 * @param fdpRequest
	 *            The request object.
	 * @param inputXml
	 *            the input xml.
	 */
	private void logHttpParameters(final Logger circleLogger, final FDPRequest fdpRequestImpl,
			final Map<String, CommandParam> xmlAsParams) {
		final StringBuilder httpParams = new StringBuilder();
		httpParams.append(LoggerUtil.getRequestAppender(fdpRequestImpl)).append("CGWINPARAM")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		for (final Map.Entry<String, CommandParam> commandParamEntry : xmlAsParams.entrySet()) {
			httpParams.append(commandParamEntry.getKey()).append("=").append(commandParamEntry.getValue().getValue())
					.append("#");
		}
		FDPLogger.info(circleLogger, getClass(), "logHttpParameters", httpParams.toString());
	}

	/**
	 * This method is used to generate the transaction id to be used
	 * 
	 * @return the transaction id.
	 */
	private Long generateTransactionId() {
		return generatorService.generateTransactionId();
	}

	/**
	 * This method is used to get the command parameter from the execution
	 * status.
	 * 
	 * @param executionStatus
	 *            The execution status.
	 * @return the command parameter.
	 */
	private CommandParam getCommandParamterFromExecutionStatus(final Status executionStatus) {
		final Integer statusValue = Status.SUCCESS.equals(executionStatus) ? FDPConstant.AIR_RECHARGE_SUCCESS_VALUE
				: FDPConstant.AIR_RECHARGE_FAILURE_VALUE;
		final CommandParamInput commandParam = new CommandParamInput(ParameterFeedType.INPUT, statusValue);
		commandParam.setName("responseCode");
		commandParam.setPrimitiveValue(Primitives.INTEGER);
		commandParam.setType(CommandParameterType.PRIMITIVE);
		commandParam.setValue(statusValue);
		return commandParam;
	}

	/**
	 * This method is used to create a command.
	 * 
	 * @param inputXML
	 *            The response xml.
	 * @param xmlAsParams
	 *            The map containing the parameters.
	 * @return the command object.
	 */
	private FDPCommand getCommand(final String inputXML, final Map<String, CommandParam> xmlAsParams) {
		final NonTransactionCommand fdpCommand = new NonTransactionCommand(FDPConstant.AIR_TRAFFIC_COMMAND_NAME);
		fdpCommand.setOutputParam(xmlAsParams);
		fdpCommand.setCommandResponse(inputXML);
		return fdpCommand;
	}
}
