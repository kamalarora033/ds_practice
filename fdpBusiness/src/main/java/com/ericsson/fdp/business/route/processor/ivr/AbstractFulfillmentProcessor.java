package com.ericsson.fdp.business.route.processor.ivr;

import java.util.List;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentRouteEnum;
import com.ericsson.fdp.business.enums.ivr.UssdHttpRouteEnum;
import com.ericsson.fdp.business.enums.ivr.commandservice.IVRCommandEnum;
import com.ericsson.fdp.business.fulfillment.service.FDPFulfillmentService;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.request.requestString.impl.FDPIVRRequestStringImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.FulfillmentResponse;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.FulfillmentUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.util.xml.XmlUtil;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

/**
 * AbstractIVRProcessor provides the common methods(like logging, response) for
 * ivr processors and an abstract method process.
 * 
 * @author Ericsson
 */
public abstract class AbstractFulfillmentProcessor implements Processor {

	/** The Constant METHOD_PROCESS. */
	protected static final String METHOD_PROCESS = "process()";

	@Resource(lookup = JNDILookupConstant.TRANSACTION_ID_GENERATOR_SERVICE)
	private TransactionSequenceGeneratorService generatorService;

	/** The Constant COUNTRY_CODE. */
	private static String COUNTRY_CODE = PropertyUtils.getProperty("COUNTRY_CODE");
	private static final Integer MSISDN_LENGTH = Integer.parseInt(PropertyUtils.getProperty("fdp.msisdn.length"));

	/** The Constant LOGGER. */
	// private static final Logger LOGGER =
	// LoggerFactory.getLogger(AbstractFulfillmentProcessor.class);

	/**
	 * Send error response.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param responseCodeEnum
	 *            the response code enum
	 * @param externalSystem
	 *            the external system
	 * @param responseCode
	 *            the response code
	 * @param responseValue
	 *            the response value
	 * @param parameters
	 *            the parameters
	 * @throws JAXBException
	 */
	protected void sendResponse(final Exchange exchange, final FulfillmentResponseCodes responseCodeEnum,
			final String externalSystem, final String responseCode, final String responseValue,
			final Object... parameters) throws JAXBException {
		String messageBody = null;
		switch (getRouteInfo(exchange)) {
		case IVR_COMMAND_SERVICE:
			messageBody = FulfillmentUtil.createIVRCommandServiceResponse(responseCodeEnum, externalSystem,
					responseCode, responseValue, parameters);
			break;
		case IVR_PRODUCT_BUY_SERVICE:
			messageBody = FulfillmentUtil.createIVRProductBuyResponse(responseCodeEnum, externalSystem, responseCode,
					responseValue, parameters);
			break;
		/*case ABILITY_SERVICE:
*/
		case FULFILLMENT_SERVICE:
			FulfillmentResponse fulfillmentResponse = FulfillmentUtil.createFullfillmentResponse(responseCodeEnum,
					externalSystem, responseCode, responseValue,
					exchange.getIn().getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class), parameters);
			updateMsisdn(exchange, fulfillmentResponse);
			messageBody = XmlUtil.getXmlUsingMarshaller(fulfillmentResponse);
			break;

		/*
		 * messageBody =
		 * FulfillmentUtil.createFullfillmentResponse(responseCodeEnum,
		 * externalSystem, responseCode, responseValue, parameters);
		 */
		default:
			break;
		}
		final Message out = exchange.getOut();
		out.setBody(messageBody);
		exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);

		printIVRTrafficOutLogger(exchange.getIn(), messageBody);
	}

	/**
	 * Send response.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param responseCodeEnum
	 *            the response code enum
	 * @param parameters
	 *            the parameters
	 * @throws JAXBException
	 */
	protected void sendResponse(final Exchange exchange, final FulfillmentResponseCodes responseCodeEnum,
			final Object... parameters) throws JAXBException {
		sendResponse(exchange, responseCodeEnum, null, null, null, parameters);
	}

	/**
	 * Gets the route info.
	 * 
	 * @param exchange
	 *            the exchange
	 * @return the route info
	 */
	final protected FulfillmentRouteEnum getRouteInfo(final Exchange exchange) {
		return FulfillmentRouteEnum.getEnumFromRouteId(exchange.getFromRouteId());
	}

	/**
	 * Send command error response.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param responseError
	 *            the fdp command
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 * @throws JAXBException
	 */
	protected void setErrorResponse(final Exchange exchange, final ResponseError responseError)
			throws ExecutionFailedException, JAXBException {
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS, "Creating failure response.");
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS,
				"sending failure response.SystemType : {}, responseCode : {}, responseValue : {}",
				responseError.getSystemType(), responseError.getResponseCode(), responseError.getResponseErrorString());
		final String externalSystem = responseError.getSystemType();
		sendResponse(exchange, FulfillmentResponseCodes.EXTERNAL_SYSTEM_ERROR, externalSystem,
				responseError.getResponseCode(), responseError.getResponseErrorString());
		logDebugMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS,
				"failure response SENT.SystemType : {}, responseCode : {}, responseValue : {}",
				responseError.getSystemType(), responseError.getResponseCode(), responseError.getResponseErrorString());
	}

	/**
	 * Log debug message in request logs.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param clazz
	 *            the clazz
	 * @param methodName
	 *            the method name
	 * @param message
	 *            the message
	 * @param parameters
	 *            the parameters
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	final protected void logDebugMessageInRequestLogs(final Exchange exchange, final Class<?> clazz,
			final String methodName, final String message, final Object... parameters) throws ExecutionFailedException {
		final ch.qos.logback.classic.Logger circleLogger = getCircleRequestLogger(exchange);
		if (circleLogger.isDebugEnabled()) {
			FDPLogger.debug(circleLogger, clazz, methodName, getMessage(exchange, message), parameters);
		}
	}

	/**
	 * Log info message in request logs.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param clazz
	 *            the clazz
	 * @param methodName
	 *            the method name
	 * @param message
	 *            the message
	 * @param parameters
	 *            the parameters
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	final protected void logInfoMessageInRequestLogs(final Exchange exchange, final Class<?> clazz,
			final String methodName, final String message, final Object... parameters) throws ExecutionFailedException {
		final ch.qos.logback.classic.Logger circleLogger = getCircleRequestLogger(exchange);
		FDPLogger.info(circleLogger, clazz, methodName, getMessage(exchange, message), parameters);
	}

	/**
	 * Checks if is valid value for key.
	 * 
	 * @param value
	 *            the value
	 * @param Key
	 *            the key
	 * @return true, if is valid value for key
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected boolean isValidListValueForKey(final String value, final String Key) throws ExecutionFailedException {
		return ApplicationCacheUtil.checkValueInCommaSeparatedList(AppCacheSubStore.CONFIGURATION_MAP, Key, value);
	}

	/**
	 * Gets the message.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param message
	 *            the message
	 * @return the message
	 */
	private String getMessage(final Exchange exchange, final String message) {
		return new StringBuilder("RID:")
				.append(exchange.getIn().getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class)).append("|")
				.append(message).toString();
	}

	/**
	 * Gets the circle request logger.
	 * 
	 * @param exchange
	 *            the exchange
	 * @return the circle request logger
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected ch.qos.logback.classic.Logger getCircleRequestLogger(final Exchange exchange)
			throws ExecutionFailedException {
		final FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		if (fdpCircle == null) {
			throw new ExecutionFailedException("{} not found in exchange so unable get circleRequestLogger.");
		}
		return getCircleRequestLogger(fdpCircle);
	}

	/**
	 * Gets the circle request logger.
	 * 
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the circle request logger
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected ch.qos.logback.classic.Logger getCircleRequestLogger(final FDPCircle fdpCircle) {
		if(fdpCircle!=null)
		{
			return LoggerUtil.getRequestLogger(fdpCircle.getCircleName(), BusinessModuleType.IVR_NORTH);	
		}
		else
		{
			return LoggerUtil.getRequestLogger(FDPLoggerFactory.DEFAULT_CIRCLE, BusinessModuleType.IVR_NORTH);	
		}
		
	}

	/**
	 * Prints the post request logs.
	 * 
	 * @param exchange
	 *            the exchange
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected void printPostRequestLogs(final Exchange exchange) throws ExecutionFailedException {
		final Message in = exchange.getIn();
		logInfoMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS,
				"OGIP:{}|LNAME:{}|CH:{}|MSISDN:{}|OREQMODE:Request",
				in.getHeader(FDPRouteHeaders.INCOMING_IP.getValue()),
				in.getHeader(FDPRouteHeaders.CHANNEL_NAME.getValue()),
				in.getHeader(FDPRouteHeaders.CHANNEL_NAME.getValue()), in.getHeader(FDPRouteHeaders.MSISDN.getValue()));
		logInfoMessageInRequestLogs(exchange, getClass(), METHOD_PROCESS,
				"OGIP:{}|LNAME:{}|CH:{}|MSISDN:{}|ORESPRSLT:Success",
				in.getHeader(FDPRouteHeaders.INCOMING_IP.getValue()),
				in.getHeader(FDPRouteHeaders.CHANNEL_NAME.getValue()),
				in.getHeader(FDPRouteHeaders.CHANNEL_NAME.getValue()), in.getHeader(FDPRouteHeaders.MSISDN.getValue()));
	}

	protected Long generateTransactionId() {
		return generatorService.generateTransactionId();
	}

	/**
	 * Check mandatory parameters.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param parameterList
	 *            the parameter list
	 * @throws JAXBException
	 */
	protected void checkMandatoryParametersAndSendResponse(final Exchange exchange,
			final List<FulfillmentParameters> parameterList) throws JAXBException, ExecutionFailedException {
		final Message in = exchange.getIn();
		final String msisdn = (String) in.getHeader("MSISDN");
		/*
		 * final FDPCircle fdpCircle =
		 * CircleCodeFinder.getFDPCircleByMsisdn(msisdn,
		 * ApplicationConfigUtil.getApplicationConfigCache());
		 */
		final FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		String errorDescription = null;
		String requestId = exchange.getIn().getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		for (final FulfillmentParameters parameter : parameterList) {
			final String value = in.getHeader(parameter.getValue(), String.class);
			if (parameter.isMandatory()) {
				if (value == null || value.isEmpty()) {
					errorDescription = new StringBuilder(FDPConstant.MSISDN)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(msisdn)
							.append(FDPConstant.LOGGER_DELIMITER)
							.append(FDPConstant.ERROR_CODE)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(FulfillmentResponseCodes.PARAMETER_MISSING.getResponseCode().toString())
							.append(FDPConstant.LOGGER_DELIMITER)
							.append(FDPConstant.ERROR_DESC)
							.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
							.append(String.format(FulfillmentResponseCodes.PARAMETER_MISSING.getDescription(),
									parameter.getValue(), fdpCircle.getCircleCode())).toString();
					FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(),
							"checkMandatoryParametersAndSendResponse()", errorDescription);
					sendResponse(exchange, FulfillmentResponseCodes.PARAMETER_MISSING, parameter.getValue());
					break;
				}
			}
			final String validationRegex = parameter.getValidationRegex();
			if (validationRegex != null && validationRegex.length()> 0 && value != null && !value.matches(validationRegex)) {
				errorDescription = new StringBuilder(FDPConstant.REQUEST_ID)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(requestId)
						.append(FDPConstant.LOGGER_DELIMITER)
						.append(FDPConstant.ERROR_CODE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(FulfillmentResponseCodes.INVALID_PARAMETER.getResponseCode().toString())
						.append(FDPConstant.LOGGER_DELIMITER)
						.append(FDPConstant.ERROR_DESC)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(String.format(FulfillmentResponseCodes.INVALID_PARAMETER.getDescription(), msisdn,
								fdpCircle.getCircleCode())).toString();
				FDPLogger.error(getCircleRequestLogger(fdpCircle), getClass(),
						"checkMandatoryParametersAndSendResponse()", errorDescription);
				sendResponse(exchange, FulfillmentResponseCodes.INVALID_PARAMETER, parameter.getValue());
				break;
			}
		}
	}

	/**
	 * This method gets the action type from request exchange.
	 * 
	 * @param exchange
	 * @return
	 */
	protected String getAction(final Exchange exchange) {
		String action = null;
		if (null != exchange && null != exchange.getIn()) {
			final Message in = exchange.getIn();
			action = in.getHeader(FulfillmentParameters.ACTION.getValue(), String.class);
		}
		return action;
	}

	/**
	 * Gets the msisdn.
	 * 
	 * @param in
	 *            the in
	 * @return the msisdn
	 */
	protected String getMsisdn(final Message in) {
		String msisdn = in.getHeader(FulfillmentParameters.MSISDN.getValue(), String.class);
		if (msisdn != null && msisdn.length() == MSISDN_LENGTH) {
			msisdn = new StringBuilder(COUNTRY_CODE).append(msisdn).toString();
		}
		return msisdn;
	}

	/**
	 * Creates the fdpivr request object.
	 * 
	 * @param in
	 *            the in
	 * @return the fullfillment request impl
	 * @throws NamingException
	 *             the naming exception
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected FulfillmentRequestImpl createFDPIVRRequestObject(final Message in) throws NamingException,
			ExecutionFailedException {
		final FulfillmentRequestImpl fullfillmentRequestImpl = new FulfillmentRequestImpl();
		final String msisdn = getMsisdn(in);
		final ChannelType channeltype = getChannelType(in);
		final String requestId = in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		final FulfillmentParameters param = FulfillmentParameters.INVOCATOR_NAME;
		final String iname = in.getHeader(param.getValue(), String.class).toUpperCase();
		fullfillmentRequestImpl.setRequestString(new FDPIVRRequestStringImpl(in.getHeader(
				FulfillmentParameters.INPUT.getValue(), String.class)));
		fullfillmentRequestImpl.setRequestId(requestId);
		// final FDPCircle fdpCircle =
		// RequestUtil.getFDPCircleFromMsisdn(msisdn.toString());
		FDPCircle fdpCircle = in.getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		fullfillmentRequestImpl.setCircle(fdpCircle);
		final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(msisdn.toString(),
				fdpCircle);
		fullfillmentRequestImpl.setCircle(fdpCircle);
		final String sessionId = requestId;
		fullfillmentRequestImpl.setSessionId(sessionId);
		fullfillmentRequestImpl.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
		fullfillmentRequestImpl.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
		fullfillmentRequestImpl.setOriginHostName(circleConfigParamDTO.getOriginHostName());
		fullfillmentRequestImpl.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
		fullfillmentRequestImpl.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
		fullfillmentRequestImpl.setChannel(channeltype);
		fullfillmentRequestImpl.setIname(iname);
		
        fullfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ORIGINAL_MSISDN, circleConfigParamDTO.getSubscriberNumber());

		return fullfillmentRequestImpl;
	}

	private ChannelType getChannelType(Message in) {	
		String iname = in.getHeader(FulfillmentParameters.INVOCATOR_NAME.getValue(), String.class);
		/*if (iname.equals("ABILITY")) {
			 return ChannelType.ABILITY;
		}else if(iname.equals("SMARTAPP")){		
			return ChannelType.SMARTAPP;
		}else if(iname.equals("EVDS")){		
			return ChannelType.EVDS;
		}else{		
			return ChannelType.IVR;
		}*/
		return ChannelType.getChannel(iname);
	}
	/**
	 * This method executes the fulfillment service.
	 * 
	 * @param fdpRequest
	 * @param jndiLookupName
	 * @param additionalInformations
	 * @return
	 * @throws ExecutionFailedException
	 */
	protected FDPResponse executeFulFillmentService(final FDPRequest fdpRequest, final String jndiLookupName,
			final Object... additionalInformations) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		try {
			final Object beanObject = ApplicationConfigUtil.getBean(jndiLookupName);
			if (beanObject instanceof FDPFulfillmentService) {
				final FDPFulfillmentService fdpFulfillmentService = (FDPFulfillmentService) beanObject;
				fdpResponse = fdpFulfillmentService.execute(fdpRequest, additionalInformations);
			}
		} catch (final NamingException e) {
			throw new ExecutionFailedException("The bean could not be found " + jndiLookupName, e);
		}
		return fdpResponse;
	}

	/**
	 * This method updates the reference txn id in response in case of
	 * exception.
	 * 
	 * @param exchange
	 * @param fulfillmentResponse
	 */
	protected void updateMsisdn(final Exchange exchange, final FulfillmentResponse fulfillmentResponse) {
		if (null != exchange.getIn().getHeader(FulfillmentParameters.TRANSACTION_ID.getValue(), String.class)) {
			fulfillmentResponse.setmsisdn(getMsisdn(exchange.getIn()));
			fulfillmentResponse.setProduct_id(exchange.getIn().getHeader(FulfillmentParameters.INPUT.getValue(),
					String.class));
		}
	}

	/**
	 * This method gets the IVR Command From Input value.
	 * 
	 * @param exchange
	 * @return
	 */
	protected IVRCommandEnum getInputIVRCommand(final Exchange exchange) {
		return IVRCommandEnum.getIVRCommandEnum(exchange.getIn().getHeader(FulfillmentParameters.INPUT.getValue(),
				String.class));
	}

	/**
	 * This method will print the out logger for IVR WEB 3PP Request.
	 * 
	 * @param in
	 * @param fulfillmentResponse
	 */
	protected void printIVRTrafficOutLogger(final Message in, final String fulfillmentResponse) {
		final String msisdn = getMsisdn(in);
		final String requestId = in.getHeader(FDPRouteHeaders.REQUEST_ID.getValue(), String.class);
		FDPLoggerFactory.reportIVRTarfficReportLogger(getClass(), "Process", requestId, msisdn, "Sending Response as :"
				+ fulfillmentResponse, FDPConstant.RESPONSE_SENT);
	}
	
	private String getInameType(Message in) {
		ChannelType channel=null;
		final FulfillmentParameters param = FulfillmentParameters.INVOCATOR_NAME;
		 String iName = in.getHeader(param.getValue(), String.class);
		if (iName.equals("ABILITY")) {
			iName=BusinessConstants.INVOCATOR_NAME_ABILITY;
		}		
		return iName;
	}
	
	/**
	 * Gets the route info.
	 * 
	 * @param exchange
	 *            the exchange
	 * @return the route info
	 */
	final protected UssdHttpRouteEnum getHttpRouteInfo(final Exchange exchange) {
		return UssdHttpRouteEnum.getEnumFromRouteId(exchange.getFromRouteId());
	}
}