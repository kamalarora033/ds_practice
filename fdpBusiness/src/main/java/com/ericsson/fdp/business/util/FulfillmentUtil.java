package com.ericsson.fdp.business.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.NotificationConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentSystemTypes;
import com.ericsson.fdp.business.enums.ivr.commandservice.FulfillmentResponseKey;
import com.ericsson.fdp.business.enums.ivr.commandservice.IVRCommandEnum;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.request.FDPCheckConsumerResponse;
import com.ericsson.fdp.business.request.FDPCheckConsumerResponseImpl;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.response.fulfillment.xml.FulfillmentResponse;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.ESFFileUtils;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.serviceprov.ValidityValueDTO;
import com.ericsson.fdp.dao.enums.SPNotificationType;
import com.ericsson.fdp.dao.enums.ValidityTypeEnum;
import com.google.gson.Gson;
import com.mtn.esf.client.ESFClientServiceImpl;

/**
 * IVRUtil class provides the utility methods for IVR.
 * 
 * @author Ericsson
 */
public class FulfillmentUtil {

	/**
	 * Instantiates a new iVR util.
	 */

	private static String filePath = PropertyUtils
			.getProperty("ability.file.path");

	private static final String url = PropertyUtils.getProperty("ability.URL");

	private FulfillmentUtil() {

	}

	/*
	 * PRODUCT BUY On success: This service returns following success code and
	 * success message.
	 * {"status":”success”,"responseCode":<responseCode>,responseDescription
	 * :<description>,systemType:<SystemName such as FDP>}
	 * 
	 * On Failure: In case of error it returns response code and response
	 * message.
	 * {"status":”failure”,"responseCode":<responseCode>,responseDescription
	 * :<description>,systemType:<SystemName such as FDP>}
	 */
	/**
	 * Creates the ivr response.
	 * 
	 * @param responseCodeEnum
	 *            the response code enum
	 * @param responseCode
	 *            the response code is nullable, If null then value of
	 *            responseCode from responseCodeEnum is used
	 * @param parameters
	 *            the parameters required to complete the message
	 * @return the string
	 */
	public static String createIVRProductBuyResponse(
			final FulfillmentResponseCodes responseCodeEnum,
			final String externalSystem, final String responseCode,
			final String responseValue, final Object... parameters) {
		final Map<String, String> responseMap = new HashMap<String, String>();
		responseMap.put(FulfillmentResponseKey.STATUS.getValue(),
				responseCodeEnum.getStatus().getValue());
		responseMap.put(FulfillmentResponseKey.RESPONSE_CODE.getValue(),
				responseCode == null ? responseCodeEnum.getResponseCode()
						.toString() : responseCode);
		responseMap.put(
				FulfillmentResponseKey.RESPONSE_DESCRIPTION.getValue(),
				getResponseDesciption(responseCodeEnum.getDescription(),
						parameters));
		responseMap.put(FulfillmentResponseKey.SYSTEM_TYPE.getValue(),
				externalSystem == null ? responseCodeEnum.getSystemType()
						: externalSystem);
		responseMap.put(FulfillmentResponseKey.RESPONSE_VALUE.getValue(),
				responseValue);
		return new Gson().toJson(responseMap);
	}

	/*
	 * Command
	 * 
	 * On success: This service returns following success code and success
	 * message.
	 * {"status":”success”,"responseCode":<responseCode>,responseDescription
	 * :<description>,systemType:<SystemName such as FDP>,
	 * responseValue:<response xml>}
	 * 
	 * On Failure: In case of error it returns response code and response
	 * message.
	 * {"status":”failure”,"responseCode":<responseCode>,responseDescription
	 * :<description>,systemType:<SystemName such as FDP>, ,
	 * responseValue:<optional, response xml for failure depending on failure is
	 * after response or before>}
	 */
	/**
	 * Creates the ivr command service response.
	 * 
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
	 * @return the string
	 */
	public static String createIVRCommandServiceResponse(
			final FulfillmentResponseCodes responseCodeEnum,
			final String externalSystem, final String responseCode,
			final String responseValue, final Object[] parameters) {
		final Map<String, String> responseMap = new HashMap<String, String>();
		responseMap.put(FulfillmentResponseKey.STATUS.getValue(),
				responseCodeEnum.getStatus().getValue());
		responseMap.put(FulfillmentResponseKey.RESPONSE_CODE.getValue(),
				responseCode == null ? responseCodeEnum.getResponseCode()
						.toString() : responseCode);
		responseMap.put(
				FulfillmentResponseKey.RESPONSE_DESCRIPTION.getValue(),
				getResponseDesciption(responseCodeEnum.getDescription(),
						parameters));
		responseMap.put(FulfillmentResponseKey.SYSTEM_TYPE.getValue(),
				externalSystem == null ? responseCodeEnum.getSystemType()
						: externalSystem);
		responseMap.put(FulfillmentResponseKey.RESPONSE_VALUE.getValue(),
				responseValue);
		return new Gson().toJson(responseMap);
	}

	public static FulfillmentResponse createFullfillmentResponse(
			final FulfillmentResponseCodes responseCodeEnum,
			final String externalSystem, final String responseCode,
			final String responseValue, final String requestId,
			final Object... parameters) {
		FulfillmentResponse fulfillmentResponse = new FulfillmentResponse();
		fulfillmentResponse.setStatus(responseCodeEnum.getStatus());
		fulfillmentResponse.setDescription(getResponseDesciption(
				responseCodeEnum.getDescription(), parameters));
		FulfillmentSystemTypes fulfillmentSystemTypes = FulfillmentSystemTypes
				.getFulfillmentSystemTypes(responseCodeEnum.getSystemType());
		fulfillmentResponse.setSystemType(fulfillmentSystemTypes);
		fulfillmentResponse.setResponseCode(String.valueOf(responseCodeEnum
				.getResponseCode()));
		fulfillmentResponse.setRequestId(requestId);
		return fulfillmentResponse;
	}

	private static String getResponseDesciption(final String description,
			final Object[] parameters) {
		return parameters.length > 0 ? String.format(description, parameters)
				: description;
	}

	/**
	 * This method updates the Command Parameter.
	 * 
	 * @param clazz
	 * @param fieldName
	 * @param object
	 * @param updatedValue
	 * @throws ExecutionFailedException
	 */
	public static void updateValueInInputCmdObject(final Class<?> clazz,
			final String fieldName, final Object object,
			final Object updatedValue) throws ExecutionFailedException {
		try {
			final Field declaredFeild = clazz.getDeclaredField(fieldName);
			if (null != declaredFeild) {
				declaredFeild.setAccessible(true);
				declaredFeild.set(object, updatedValue);
			}
		} catch (NoSuchFieldException e) {
			if (!clazz.getSuperclass().equals(Object.class)) {
				updateValueInInputCmdObject(clazz.getSuperclass(), fieldName,
						object, updatedValue);
			}
			throw new ExecutionFailedException(
					"The value of the parameter could not be updated.", e);
		} catch (SecurityException e) {
			throw new ExecutionFailedException(
					"The value of the parameter could not be updated.", e);
		} catch (IllegalArgumentException e) {
			throw new ExecutionFailedException(
					"The value of the parameter could not be updated.", e);
		} catch (IllegalAccessException e) {
			throw new ExecutionFailedException(
					"The value of the parameter could not be updated.", e);
		}
	}

	/**
	 * This method removes the "negotiatedCapabilities" from command.
	 * 
	 * @param fdpCommand
	 * @param parameterName
	 * @throws ExecutionFailedException
	 */
	public static void removeFromCommand(final FDPCommand fdpCommand,
			final String parameterName) throws ExecutionFailedException {
		CommandParam commandParamToRemove = fdpCommand
				.getInputParam(parameterName);
		List<CommandParam> paramInputs = fdpCommand.getInputParam();
		if (null != commandParamToRemove) {
			paramInputs.remove(commandParamToRemove);
		}
	}

	/**
	 * This method configures the parameter values for command execution.
	 * 
	 * @param fdpRequest
	 * @param commandEnum
	 * @throws EvaluationFailedException
	 */
	public static void preCommandExecutor(
			final FulfillmentRequestImpl fdpRequest,
			final IVRCommandEnum commandEnum, final FDPCommand fdpCommand,
			final Logger logger) throws ExecutionFailedException {
		if (logger.isDebugEnabled()) {
			printParams(fdpCommand, fdpRequest, "Before", logger);
		}
		switch (commandEnum) {
		case DELETE_OFFER:
			deleteOfferPreCommandExecutor(fdpRequest, commandEnum, fdpCommand,
					logger);
			break;
		case UPDATE_OFFER:
			updateOfferPreCommandExecutor(fdpRequest, commandEnum, fdpCommand,
					logger);
			break;
		default:
			break;
		}
		if (logger.isDebugEnabled()) {
			printParams(fdpCommand, fdpRequest, "After", logger);
		}
	}

	/**
	 * This method handle delete Offer.
	 * 
	 * @param fdpRequest
	 * @param commandEnum
	 * @param fdpCommand
	 * @throws ExecutionFailedException
	 */
	private static void deleteOfferPreCommandExecutor(
			final FulfillmentRequestImpl fdpRequest,
			final IVRCommandEnum commandEnum, final FDPCommand fdpCommand,
			final Logger logger) throws ExecutionFailedException {
		alterMsisdn(fdpRequest, fdpCommand, logger);
		// Updating OfferID.
		alterCmdParam(fdpRequest, fdpCommand, FulfillmentParameters.OFFER_ID,
				logger);
		// Remove "negotiatedCapabilities" in case if it configured from GUI.
		final List<String> toRemoveCommandParams = new ArrayList<String>();
		List<String> allowedCommandParams = getCommandAllowedParamsList(
				fdpRequest, commandEnum.getIvrName(), logger);
		if (null != allowedCommandParams) {
			List<CommandParam> commandParamInputs = fdpCommand.getInputParam();
			for (CommandParam commandParam : commandParamInputs) {
				if (commandParam instanceof CommandParamInput
						&& !allowedCommandParams.contains(commandParam
								.getName())) {
					toRemoveCommandParams.add(commandParam.getName());
				}
			}
		}
		for (final String paramsToRemove : toRemoveCommandParams) {
			FDPLogger.debug(logger, FulfillmentUtil.class,
					"deleteOfferPreCommandExecutor()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "Removing Paramete with name" + paramsToRemove);
			FulfillmentUtil.removeFromCommand(fdpCommand, paramsToRemove);
		}
	}

	/**
	 * This method handles update Offer.
	 * 
	 * @param fdpRequest
	 * @param commandEnum
	 * @param fdpCommand
	 * @throws ExecutionFailedException
	 */
	private static void updateOfferPreCommandExecutor(
			final FulfillmentRequestImpl fdpRequest,
			final IVRCommandEnum commandEnum, final FDPCommand fdpCommand,
			final Logger logger) throws ExecutionFailedException {
		alterMsisdn(fdpRequest, fdpCommand, logger);
		// Updating OfferID.
		alterCmdParam(fdpRequest, fdpCommand, FulfillmentParameters.OFFER_ID,
				logger);
		// Updating OfferType.
		alterCmdParam(fdpRequest, fdpCommand, FulfillmentParameters.OFFER_TYPE,
				logger);
		// Updating expiryDate
		alterCmdParam(fdpRequest, fdpCommand,
				FulfillmentParameters.EXPIRY_DATE, logger);
		// Updating startDate.
		alterStartDate(fdpRequest, fdpCommand, logger);
		final List<String> toRemoveCommandParams = new ArrayList<String>();
		List<String> allowedCommandParams = getCommandAllowedParamsList(
				fdpRequest, commandEnum.getIvrName(), logger);
		if (null != allowedCommandParams) {
			List<CommandParam> commandParamInputs = fdpCommand.getInputParam();
			for (CommandParam commandParam : commandParamInputs) {
				if (commandParam instanceof CommandParamInput
						&& !allowedCommandParams.contains(commandParam
								.getName())) {
					toRemoveCommandParams.add(commandParam.getName());
				}
			}
		}
		for (final String paramsToRemove : toRemoveCommandParams) {
			FDPLogger.debug(logger, FulfillmentUtil.class,
					"updateOfferPreCommandExecutor()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "Removing Paramete with name" + paramsToRemove);
			FulfillmentUtil.removeFromCommand(fdpCommand, paramsToRemove);
		}
	}

	/**
	 * This method alter the command input params.
	 * 
	 * @param fulfillmentRequestImpl
	 * @param fdpCommand
	 * @param fulfillmentParameters
	 * @throws ExecutionFailedException
	 */
	private static void alterCmdParam(
			final FulfillmentRequestImpl fulfillmentRequestImpl,
			final FDPCommand fdpCommand,
			final FulfillmentParameters fulfillmentParameters,
			final Logger logger) throws ExecutionFailedException {
		CommandParamInput commandParamInput = (CommandParamInput) fdpCommand
				.getInputParam(fulfillmentParameters.getValue());
		if (null != commandParamInput && null != fulfillmentParameters) {
			Object newValue = null;
			switch (fulfillmentParameters) {
			case OFFER_ID:
			case OFFER_TYPE:
				newValue = fulfillmentRequestImpl
						.getCommandInputParams(fulfillmentParameters);
				break;
			case EXPIRY_DATE:
				ValidityValueDTO validityValueDTO = new ValidityValueDTO();
				validityValueDTO.setDays(Integer.valueOf(fulfillmentRequestImpl
						.getCommandInputParams(fulfillmentParameters)));
				validityValueDTO
						.setValidityType(ValidityTypeEnum.APPLY_FROM_TODAY_PLUS);
				newValue = CommandParamInputUtil
						.evaluateValidity((ValidityValueDTO) validityValueDTO);
			default:
				break;
			}
			if (null != newValue) {
				FDPLogger.debug(logger, FulfillmentUtil.class,
						"alterCmdParam()",
						LoggerUtil.getRequestAppender(fulfillmentRequestImpl)
								+ "Updating Old value from "
								+ commandParamInput.getValue() + " to "
								+ newValue);
				FulfillmentUtil.updateValueInInputCmdObject(
						CommandParamInput.class,
						FDPConstant.DEFINED_VALUE_TEXT, commandParamInput,
						newValue);
			}
		}
	}

	/**
	 * This method alters the subscriberNumber in commands.
	 * 
	 * @param fulfillmentRequestImpl
	 * @param fdpCommand
	 * @throws ExecutionFailedException
	 */
	private static void alterMsisdn(
			final FulfillmentRequestImpl fulfillmentRequestImpl,
			final FDPCommand fdpCommand, final Logger logger)
			throws ExecutionFailedException {
		CommandParamInput commandParamInput = (CommandParamInput) fdpCommand
				.getInputParam("subscriberNumber");
		if (null != commandParamInput
				&& !commandParamInput.getDefinedValue().equals(
						"subscriberNumber")) {
			FDPLogger.debug(
					logger,
					FulfillmentUtil.class,
					"alterMsisdn()",
					LoggerUtil.getRequestAppender(fulfillmentRequestImpl)
							+ "Updating Old value from "
							+ commandParamInput.getValue() + " to "
							+ fulfillmentRequestImpl.getSubscriberNumber());
			FulfillmentUtil.updateValueInInputCmdObject(
					CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
					commandParamInput, String.valueOf(fulfillmentRequestImpl
							.getSubscriberNumber()));
		}
	}

	/**
	 * This method prints.
	 * 
	 * @param fdpCommand
	 * @param fdpRequest
	 * @param state
	 */
	private static void printParams(final FDPCommand fdpCommand,
			final FDPRequest fdpRequest, final String state, final Logger logger) {
		for (final CommandParam commandParam : fdpCommand.getInputParam()) {
			if (commandParam instanceof CommandParamInput) {
				CommandParamInput commandParamInput = (CommandParamInput) commandParam;
				FDPLogger.debug(
						logger,
						FulfillmentUtil.class,
						"printParams()",
						LoggerUtil.getRequestAppender(fdpRequest)
								+ "Input Param Name: "
								+ commandParamInput.getName() + " Value: "
								+ commandParamInput.getDefinedValue() + ", "
								+ state);
			}
		}
	}

	/**
	 * This methods fetches the configuration of all allowed parameters.
	 * 
	 * @param fdpRequest
	 * @param ivrName
	 * @return
	 */
	private static List<String> getCommandAllowedParamsList(
			final FDPRequest fdpRequest, final String ivrName,
			final Logger logger) {
		final IVRCommandEnum commandEnum = IVRCommandEnum
				.getIVRCommandEnum(ivrName);
		String confiText = null;
		if (null != commandEnum) {
			String key = fdpRequest.getCircle().getCircleCode()
					+ FDPConstant.UNDERSCORE + commandEnum.getIvrName();
			FDPLogger.debug(logger, FulfillmentUtil.class,
					"getCommandAllowedParamsList()",
					LoggerUtil.getRequestAppender(fdpRequest)
							+ "Looking for Key:" + key);
			confiText = PropertyUtils.getProperty(key);
		}
		return (null != confiText ? Arrays.asList(confiText
				.split(FDPConstant.COMMA)) : new ArrayList<String>());
	}

	/**
	 * This method alters the start Date.
	 * 
	 * @param fulfillmentRequestImpl
	 * @param fdpCommand
	 * @throws ExecutionFailedException
	 */
	private static void alterStartDate(
			final FulfillmentRequestImpl fulfillmentRequestImpl,
			final FDPCommand fdpCommand, final Logger logger)
			throws ExecutionFailedException {
		CommandParamInput commandParamInput = (CommandParamInput) fdpCommand
				.getInputParam("startDate");
		if (null != commandParamInput) {
			Object newValue = null;
			ValidityValueDTO validityValueDTO = new ValidityValueDTO();
			validityValueDTO.setValidityType(ValidityTypeEnum.NOW);
			newValue = CommandParamInputUtil
					.evaluateValidity((ValidityValueDTO) validityValueDTO);
			FDPLogger.debug(
					logger,
					FulfillmentUtil.class,
					"alterStartDate()",
					LoggerUtil.getRequestAppender(fulfillmentRequestImpl)
							+ "Updating Old value from "
							+ commandParamInput.getValue() + " to " + newValue);
			FulfillmentUtil.updateValueInInputCmdObject(
					CommandParamInput.class, FDPConstant.DEFINED_VALUE_TEXT,
					commandParamInput, newValue);
		}
	}


	/**
	 * This method will swap the msisdn for consumer GAD execution.
	 * 
	 * @param msisdnToUpdateInRequest
	 * @param fdpRequest
	 */
	public static void swapMsisdn(final Long msisdnToUpdateInRequest, final FDPRequest fdpRequest) {
		((FDPRequestImpl) fdpRequest).setSubscriberNumber(msisdnToUpdateInRequest);
	}
	
	
	/**
	 * This method checks the service class present in postpaid circle configuration.
	 * @param fdpRequest
	 * @param fdpCommand
	 * @return
	 */
	private static boolean isPostPaidServiceClass(final FDPRequest fdpRequest, final FDPCommand fdpCommand) {
		boolean isPostPaidServiceClass = false;
		String serviceClass = null;
		final String cachePostPaidServiceClass = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.POSTPAID_SERVICE_CLASS.getAttributeName());
		if (null != cachePostPaidServiceClass && cachePostPaidServiceClass.length() > 0) {
			for (final String regex : cachePostPaidServiceClass.trim().split(FDPConstant.COMMA)) {
				serviceClass = getServiceClass(fdpCommand);
				if (null != serviceClass & Pattern.matches(regex, serviceClass)) {
					isPostPaidServiceClass = true;
					break;
				}
			}
		}
		return isPostPaidServiceClass;
	}

	/**
	 * This method gets the user service class.
	 * 
	 * @param fdpCommand
	 * @return
	 */
	private static String getServiceClass(FDPCommand fdpCommand) {
		CommandParam commandParam = fdpCommand.getOutputParam("serviceClassCurrent");
		return (null != commandParam) ? commandParam.getValue().toString() : null;
	}

	/**
	 * This method is use to call the fulfillment api for the air notification
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static String checkIfInputMsisdnIsValid(final FDPRequest fdpRequest, final String consumerMsisdn) throws ExecutionFailedException {
		String responseText = null;
			final FDPCheckConsumerResponse fdpCheckConsumerResp = isConsumerPrepaid(fdpRequest, consumerMsisdn);
			responseText = (Status.SUCCESS.equals(fdpCheckConsumerResp.getExecutionStatus()) && fdpCheckConsumerResp
					.isPrePaidConsumer()) ? responseText : getNotificationTextFromConfiguration(fdpRequest,
				ConfigurationKey.SHARED_BONUD_BUNDLE_POSTPAID_NOTI_TEXT_CONSUMER);
		//} catch (final NotificationFailedException e) {
		//	throw new ExecutionFailedException("Could not create notification text", e);
		//}
		/*final String isConsumerExistText = "Consumer already exist, with some other provider";
		responseText = (null == responseText) ? (isConsumerTypeNew(fdpRequest, consumerMsisdn) ? null
				: isConsumerExistText) : responseText;*/
		return responseText;
	}
	
	/**
	 * This method is used to read ESF data and to send record on Ability
	 */
	public static void sendESFData(final FDPCircle fdpCircle)
			throws IOException, ExecutionFailedException {

		final Logger logger = FDPLoggerFactory.getRequestLogger(
				fdpCircle.getCircleName(), BusinessModuleType.ABILITY.name());

		Date date = new Date();
		final SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
		String filePathFormedHourly = filePath + FDPConstant.HOURLY + ".txt";
		String filePathDaily = filePath + dateFormat.format(date) + ".txt";

		ESFClientServiceImpl client = new ESFClientServiceImpl();
		ESFFileUtils esfData = new ESFFileUtils();
		List<Map<String, String>> listContent = esfData
				.readESFLogFile(filePathFormedHourly);
		logger.debug("Read the " + filePathFormedHourly
				+ " file and stored the content in list");
		File file = new File(filePathFormedHourly);
		file.delete();
		for (Map<String, String> map : listContent) {
			boolean result = client.updateAbility(map,
					fdpCircle.getCircleCode(), url);
			if (result) {
				logger.debug("Data has sent to Ability server is successfully for the map :: "
						+ map);
			} else {
				esfData.writeESFLogFile(filePathDaily, map);
				logger.debug("Unable to sent data to ESF server !! for the map :: "
						+ map);
			}
		}

	}

	/**
	 * This method is use to call the fulfillment api for the air notification
	 * 
	 * @param productName
	 * @param msisdn
	 * @param circleCode
	 * @param iName
	 * @param logger
	 * @return the fulfillment response for the air notification PAM
	 * @throws IOException
	 */
	public static String postFulfillmentAirNotification(
			final String productName, final Long msisdn,
			final String circleCode, final String iName,
			final Logger logger) throws IOException {
		StringBuilder request = new StringBuilder();
		request.append(PropertyUtils
				.getProperty("fdp.fulfillment.airnotification.url"));
		request.append(FDPConstant.QUESTIONMARK+"input=" + FDPConstant.NORMAL_PRODUCT_PREFIX
				+ FDPConstant.UNDERSCORE + URLEncoder.encode(productName, "UTF-8"));
		if (null != msisdn)
			request.append(FDPConstant.AMPERSAND + FDPConstant.MSISDN + "="
					+ msisdn);
		if (null != circleCode)
			request.append("&circlecode=" + circleCode);
		request.append("&username="
				+ PropertyUtils.getProperty("fdp.fulfillment.userName")
				+ "&password="
				+ PropertyUtils.getProperty("fdp.fulfillment.password"));
		if (null != iName)
			request.append(FDPConstant.AMPERSAND +"iname=" + iName);
		URL fulfillmentUrl = new URL(request.toString());
		HttpURLConnection httpUrlConnection = (HttpURLConnection) fulfillmentUrl
				.openConnection();

		logger.debug("\nSending 'POST' request to URL : " + request);
		logger.trace("Response Code : " + httpUrlConnection.getResponseCode());

		BufferedReader in = new BufferedReader(new InputStreamReader(
				httpUrlConnection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();
	}
	
	/**
	 * This method will prepare the custom notification text for the SP Execution.
	 * 
	 * @param fdpRequest
	 * @param circleLogger
	 * @param fulfillmentResponse
	 * @param fdpResponse
	 * @return
	 * @throws ExecutionFailedException
	 * @throws NotificationFailedException 
	 */
	public static String getCustomFulfillmentNotificationText(final FDPRequest fdpRequest, final Logger circleLogger,
			FulfillmentResponse fulfillmentResponse, final FDPResponse fdpResponse) throws ExecutionFailedException {
		String notificationText = null;
		try {
			if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE) && fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE) instanceof Long) {
				final Long notificationId = (Long) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE);
				notificationText= (notificationId != null) ? TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger) : notificationText;
			}
		} catch (NotificationFailedException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Failed to create notification, Actual Error:",e);
		}
		return notificationText;
	}
	
	/**
	 * To get the custom notification text, from AUXParam.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static String getNotificationIdForCustomFulfillmentNotificationText(final FDPRequest fdpRequest, final SPNotificationType spNotificationType,final Logger circleLogger)
			throws ExecutionFailedException{
		String notificationText = null;
		try {
			if(null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE) && fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE) instanceof Map<?,?>) {
				final Long notificationId = ((Map<SPNotificationType,Long>)(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.NOTIFICATION_TO_OVERRIDE))).get(spNotificationType);
				notificationText= (notificationId != null) ? TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger) : notificationText;
			}
		} catch (NotificationFailedException e) {
			e.printStackTrace();
			throw new ExecutionFailedException("Failed to create notification for types, Actual Error:",e);
		}
		return notificationText;
	}

	/**
	 * This method will execute the command and find the out param value
	 * @param fdpRequest
	 * @param paramValue
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static CommandParamOutput getCommandOutput(final FDPRequest fdpRequest, final String parameterPath,
			final String commandName, boolean addToRequest) throws ExecutionFailedException {
		FDPCommand fdpCommand = fdpRequest.getExecutedCommand(commandName);
		if (null == fdpCommand) {
			addToRequest = true;
			final FDPCacheable fdpCacheable = (FDPCommand) ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, commandName));
			if (null != fdpCacheable && fdpCacheable instanceof FDPCommand) {
				fdpCommand = (FDPCommand) fdpCacheable;
				if (Status.SUCCESS.equals(fdpCommand.execute(fdpRequest)) && addToRequest) {
					fdpRequest.addExecutedCommand(fdpCommand);
				}
			}
		}
		return (CommandParamOutput) fdpCommand.getOutputParam(parameterPath);
	}
	/**
	 * This method will check if consumer limit reached.
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public static boolean isSharedBonusBundleConsumerLimitReached(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		boolean isLimitReached = true;
		String max_shared_bb_allowed_consumer = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SHARED_BONUD_BUNDLE_MAX_ALLOWED_CONSUMERS.getAttributeName());
		int max_shared_bb_allowed_consumer_int = (null == max_shared_bb_allowed_consumer) ? 3 : Integer
				.parseInt(max_shared_bb_allowed_consumer);
		final List<String> consumerMsisdnList = getConsumerList(fdpRequest,true);
		isLimitReached = (null != consumerMsisdnList) ? max_shared_bb_allowed_consumer_int <= consumerMsisdnList
				.size() : isLimitReached;
		return isLimitReached;
	}
	
	/**
	 * This method will get the notification text if limit of consumer reached.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static String getSharedBonusBundleConsumerLimitReachText(final FDPRequest fdpRequest)
			throws ExecutionFailedException {
		return (isSharedBonudBundleProviderOrNew(fdpRequest, FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE)
				&& null != fdpRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.SHARED_BONUS_BUNDLE_USER_TYPE_PROVIDER)
				&& FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(0)
						.equals(fdpRequest
								.getAuxiliaryRequestParameter(AuxRequestParam.SHARED_BONUS_BUNDLE_USER_TYPE_PROVIDER)) && isSharedBonusBundleConsumerLimitReached(fdpRequest)) ? getNotificationTextFromConfiguration(
				fdpRequest, ConfigurationKey.SHARED_BONUD_BUNDLE_MAX_CONSUMER_TEXT) : null;
	}
	
	/**
	 * This method will check if 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static boolean isSharedBonudBundleProviderOrNew(final FDPRequest fdpRequest, final List<String> types) throws ExecutionFailedException {
		final String parameterPath = "type";
		final CommandParamOutput commandParamOutput = getCommandOutput(fdpRequest, parameterPath,
				FDPConstant.SBB_GETDETAILS_COMMAND, true);
		boolean isProvider = (null != commandParamOutput ? types
				.contains(commandParamOutput.getValue().toString()) : false);
		if (isProvider && fdpRequest instanceof FDPRequestImpl) {
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SHARED_BONUS_BUNDLE_USER_TYPE_PROVIDER,
					commandParamOutput.getValue().toString());
		}
		return isProvider;
	}
	
	/**
	 * This method will get all consumer list
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	public static List<String> getConsumerList(final FDPRequest fdpRequest, final boolean deleteAllowedOnly) throws ExecutionFailedException {
		final String parameterPath = "consumerList";
		List<String> consumerList = null;
		if(deleteAllowedOnly && fdpRequest instanceof FulfillmentRequestImpl) {
			final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SBB_DELETE_ALLOWED, Boolean.TRUE);
		}
		final CommandParamOutput commandParamOutput = getCommandOutput(fdpRequest, parameterPath,
				FDPConstant.SBB_GETDETAILS_COMMAND,true);
		if(null != commandParamOutput) {
			final String commandSeperatedConsumerList = commandParamOutput.getValue().toString();
			consumerList = (null != commandSeperatedConsumerList && !FDPConstant.EMPTY_STRING.equals(commandSeperatedConsumerList)) ? new ArrayList<String>(
					Arrays.asList(commandSeperatedConsumerList.split(FDPConstant.COMMA))) : new ArrayList<String>();
		}
		return consumerList;
	}
	
	/**
	 * This method will check if consumer exist in DB.
	 * 
	 * @param fdpRequest
	 * @param consumerMsisdn
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public static boolean isConsumerTypeNew(final FDPRequest fdpRequest, final String consumerMsisdn)
			throws ExecutionFailedException {
		boolean isNewType = false;
		final Object tempValue = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN);
		try {
			if (fdpRequest instanceof FDPRequestImpl) {
				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, consumerMsisdn);
				final FDPCacheable fdpCacheable = (FDPCommand) ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, FDPConstant.SBB_GETDETAILS_COMMAND));
				if (null != fdpCacheable && fdpCacheable instanceof FDPCommand) {
					FDPCommand fdpCommand = (FDPCommand) fdpCacheable;
					if (null != fdpCommand && Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
						isNewType = (null != fdpCommand.getOutputParam("type")) ? FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE
								.get(1).equals(fdpCommand.getOutputParam("type").getValue().toString()) : isNewType;
					}
				}
			}
		} finally {
			if (fdpRequest instanceof FDPRequestImpl) {
				final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
				fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN, tempValue);
			}
		}
		return isNewType;
	}
	
	/**
	 * This method will check if deletion request is self delete.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	public static boolean isConsumerSelfDeleteRequest(final FDPRequest fdpRequest) {
		final FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
		return fulfillmentRequestImpl.getSubscriberNumber().toString()
				.equals(fulfillmentRequestImpl.getCommandInputParams(FulfillmentParameters.CONSUMER_MSISDN));
	}
	
		/**
	 * This method will execute the GAD for consumer msisdn.
	 * 
	 * @param fdpRequest
	 * @param consumerMsisdn
	 * @return
	 * @throws ExecutionFailedException 
	 */
	private static FDPCheckConsumerResponse isConsumerPrepaid(final FDPRequest fdpRequest, final String consumerMsisdn) throws ExecutionFailedException {
		FDPCommand fdpCommand = null;
		final Long requestorMsisdn = fdpRequest.getSubscriberNumber();
		FDPCheckConsumerResponse fdpCheckConsumerResponse = new FDPCheckConsumerResponseImpl();
		String getAccountDetails = Command.GETACCOUNTDETAILS.getCommandDisplayName();
		try {
			final FDPCacheable fdpCacheable = (FDPCommand) ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND, getAccountDetails));
			if(null != fdpCacheable && fdpCacheable instanceof FDPCommand) {
				fdpCommand = (FDPCommand) fdpCacheable;
				swapMsisdn(Long.valueOf(consumerMsisdn), fdpRequest);
				if(Status.SUCCESS.equals(fdpCommand.execute(fdpRequest))) {
					FDPCheckConsumerResponseImpl consumerResponseImpl = (FDPCheckConsumerResponseImpl) fdpCheckConsumerResponse;
					consumerResponseImpl.setPrePaidConsumer(!isPostPaidServiceClass(fdpRequest, fdpCommand));
					consumerResponseImpl.setStatus(Status.SUCCESS);
				}
			}
		} finally {
			swapMsisdn(requestorMsisdn, fdpRequest);
		}
		return fdpCheckConsumerResponse;
	}
	
	/**
	 * This method will get the configuration.
	 * 
	 * @param fdpRequest
	 * @param configurationKey
	 * @return
	 */
	public static String getNotificationTextFromConfiguration(final FDPRequest fdpRequest,
			final ConfigurationKey configurationKey) {
		String promptText = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(configurationKey.getAttributeName());
		return promptText;
	}
	
	/**
	 * This method set cost paramters in case of Time4U.
	 * 
	 * @param fdpRequest
	 * @param time4u_cost
	 */
	public static void updateParameterForTime4U(final FDPRequest fdpRequest, final int time4u_cost) {
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TIME4_PRODUCT_COST_EVD_MOMO,
				time4u_cost);
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TIME4_PRODUCT_COST,
				(time4u_cost * 1000));
	}
}
