package com.ericsson.fdp.business.util;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.TariffEnquiryNotificationOptions;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.business.vo.FDPNotificationVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.FDPCSAttributeParam;
import com.ericsson.fdp.common.enums.FDPCSAttributeValue;
import com.ericsson.fdp.common.enums.FDPSPNotificationParamEnum;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

/**
 * This class is the utility class for notification creation.
 * 
 * @author Ericsson
 * 
 */
public class TariffEnquiryNotificationUtil extends NotificationUtil {

	/**
	 * Instantiates a new notification util.
	 */
	private static String EXPIRY_DATE = PropertyUtils.getProperty("EXPIRY_DATE");
	protected TariffEnquiryNotificationUtil() {

	}
	
	/**
	 * This method is used to send the notification.
	 * 
	 * @param fdpRequest
	 *            The request for which the notification is to be sent.
	 * @param notificationId
	 *            The id for which the notification is to be sent.
	 * @return Notification text created.
	 * @throws NotificationFailedException
	 *             Exception, if any in sending the notification.
	 */
	public static String createNotificationText(final FDPRequest fdpRequest, final Long notificationId,
			final Logger circleLogger) throws NotificationFailedException {
        return createNotificationText(fdpRequest, notificationId, circleLogger, null);
	}
	
	public static String createNotificationText(final FDPRequest fdpRequest, final Long notificationId,
 final Logger circleLogger,
            FDPSPNotificationParamEnum fdpspNotificationParamEnum) throws NotificationFailedException {

		String notificationText = null;
		try {
			circleLogger.debug(LoggerUtil.getRequestAppender(fdpRequest) + "The notification id to be used "
					+ notificationId);
			if (notificationId != null) {
				final Object fdpNotificationDTOObj = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.NOTIFICATION, notificationId));
				if (fdpNotificationDTOObj instanceof FDPNotificationVO) {
					//final FDPNotificationVO fdpNotificationDTO = (FDPNotificationVO) fdpNotificationDTOObj;
					final FDPNotificationVO fdpNotificationDTO = getFDPNotificationVOForOtherLanguage((FDPNotificationVO) fdpNotificationDTOObj, fdpRequest);
					FDPLogger.debug(circleLogger, NotificationUtil.class, "createNotificationText()",
							LoggerUtil.getRequestAppender(fdpRequest) + "NotificationVO found " + fdpNotificationDTO);
					final Map<String, CommandParam> parametersToReplace = fdpNotificationDTO.getParametersToReplace();
					if(!checkIfBalanceOtherDetailsConfigured(parametersToReplace) && checkFailureReasonPresent(fdpRequest)) {
						return failureReasonNotificationId(fdpRequest, circleLogger);
					}
					if(ServiceProvisioningUtil.isProductSpTypeValid(fdpRequest, FDPServiceProvSubType.BALANCE_ENQUIRY)){
						ServiceProvisioningUtil.updateSubscriberInRequestForBeneficiary(fdpRequest, true, circleLogger);
					}
				//	final Map<String, String> values = processParameters(fdpRequest, parametersToReplace, circleLogger);
					final Map<String, String> values = processParameters(fdpRequest, parametersToReplace, circleLogger);	
					//commenetd by Rahul , will be uncomment once get to know the reason of using this code
					/*if(values.size()!=0 || null!=values){
						for(Map.Entry<String, String> entry : values.entrySet()){
							if(entry.getKey().startsWith(EXPIRY_DATE)){	
								SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssz");
								Date date = formatter.parse(entry.getValue().toString());
								formatter.applyPattern("dd/MM/yyyy HH:mm");
								values.put(entry.getKey(), formatter.format(date));
							}					
						}
					}*/
					notificationText = processTemplate(fdpNotificationDTO.getMessage(), values, circleLogger);
					notificationText = getNotificationTextForSharedAccount(fdpRequest, notificationText, fdpNotificationDTO);
                    if (fdpspNotificationParamEnum != null) {
                        notificationText = NotificationUtil.getModifiedNotificationText(fdpRequest, notificationText,
                                fdpspNotificationParamEnum);
                    }
					FDPLogger.debug(circleLogger, NotificationUtil.class, "createNotificationText()",
							LoggerUtil.getRequestAppender(fdpRequest) + "Notification text formed " + notificationText);
					String notificationTextforLog = new String(notificationText);
					notificationTextforLog = notificationTextforLog.replace("\n", "").replace("\r", "");
					FDPLogger.info(circleLogger, NotificationUtil.class, "createNotificationText()",
							LoggerUtil.getRequestAppender(fdpRequest) + "NOTIF"
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + notificationTextforLog
									+ FDPConstant.LOGGER_DELIMITER + "TID" + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
									+ fdpNotificationDTO.getName()
									+ FDPConstant.LOGGER_DELIMITER + FDPLoggerConstants.CORRELATION_ID
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER 
									+ fdpRequest.getOriginTransactionID().toString());
				}
			}
			
		} catch (final ExecutionFailedException e) {
			FDPLogger.error(circleLogger, NotificationUtil.class, "createNotificationText()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Could not send notification", e);
			throw new NotificationFailedException("Could not send notification", e);
		} catch (final IOException e) {
			FDPLogger.error(circleLogger, NotificationUtil.class, "createNotificationText()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Could not create notification", e);
			throw new NotificationFailedException("Could not create notification", e);
		} /*catch (ParseException e){
			FDPLogger.error(circleLogger, NotificationUtil.class, "createNotificationText()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Could not create notification", e);
			throw new NotificationFailedException("Could not create notification", e);
		}*/
		return notificationText;
	}

	/**
	 * This method would get the notification text for shared account customer.
	 */
	private static String getNotificationTextForSharedAccount(final FDPRequest fdpRequest, String notificationText,
			final FDPNotificationVO fdpNotificationDTO) throws ExecutionFailedException {
		
		String thresholdMinusCounterText = PropertyUtils.getProperty(
				SharedAccountConstants.THRESHOLD_MINUS_COUNTER_CONSTANT);
		
		if (thresholdMinusCounterText != null && fdpNotificationDTO != null && fdpNotificationDTO.getText() != null 
				&& fdpNotificationDTO.getText().contains(thresholdMinusCounterText)) {
			
			FDPCommand commandName = fdpRequest.getExecutedCommand(
										Command.GET_USAGE_THRESHOLDS_AND_COUNTERS_CONSUMER.getCommandDisplayName());
			String usageCounterId = null;
			String usageThresholdId = null;

			if (commandName == null && FulfillmentUtil.isSharedBonudBundleProviderOrNew(fdpRequest,
					Arrays.asList(SharedAccountConstants.PROVIDER_AUX_VALUE))) {
				usageCounterId = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UC_ID) == null ? SharedAccountConstants.PROVIDER_UC_ID
						: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UC_ID);
				usageThresholdId =  FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UT_ID) == null ? SharedAccountConstants.PROVIDER_UT_ID
								: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_PROVIDER_UT_ID);
			} else {
				usageCounterId =  FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UC_ID) == null ? SharedAccountConstants.CONSUMER_UC_ID
						: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UC_ID);
				usageThresholdId = FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UT_ID) == null ? SharedAccountConstants.CONSUMER_UT_ID
						: FulfillmentUtil.getNotificationTextFromConfiguration(fdpRequest,ConfigurationKey.SHARED_BONUS_BUNDLE_CONSUMER_UT_ID);
			}
			
			if (commandName == null) {
				commandName = fdpRequest.getExecutedCommand(Command.GET_USAGE_THRESHOLDS_AND_COUNTERS.getCommandDisplayName());
			}
			
			Map<String, Object> uMap = MVELUtil.evaluateUCUTDetailsForUser(fdpRequest, commandName);
			if(uMap!=null && uMap.containsKey(FDPCSAttributeValue.UC.name()+ FDPConstant.UNDERSCORE + usageCounterId + FDPConstant.UNDERSCORE 
			+ FDPCSAttributeParam.VALUE.name().toString()) && uMap.containsKey(FDPCSAttributeValue.UT.name()	+ FDPConstant.UNDERSCORE + usageThresholdId + FDPConstant.UNDERSCORE
							+ FDPCSAttributeParam.VALUE.name())){
			Integer usageCounter = Integer.parseInt((String) uMap.get(FDPCSAttributeValue.UC.name()
							+ FDPConstant.UNDERSCORE + usageCounterId + FDPConstant.UNDERSCORE 
							+ FDPCSAttributeParam.VALUE.name().toString()));
			Integer usageThreshold = Integer.parseInt((String) uMap.get(FDPCSAttributeValue.UT.name()
							+ FDPConstant.UNDERSCORE + usageThresholdId + FDPConstant.UNDERSCORE
							+ FDPCSAttributeParam.VALUE.name()));
			String dataValue = String.valueOf(usageThreshold - usageCounter);
			notificationText = notificationText.replace(thresholdMinusCounterText, dataValue);
			}
		}
		return notificationText;
	}

	/**
	 * This method is used to process the parameters and find the value of the
	 * parameter.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @param parametersToReplace
	 *            The parameters to replace.
	 * @param circleLogger
	 * 		the logger 	
	 * @throws NotificationFailedException
	 *      Exception, if parameter value could not be evaluated.
	 * @return values map of parameter with values
	 */
	protected static Map<String, String> processParameters(final FDPRequest fdpRequest,
			final Map<String, CommandParam> parametersToReplace, final Logger circleLogger)
			throws NotificationFailedException {
		final Map<String, String> values = new HashMap<String, String>();
		try {
			for (final Map.Entry<String, CommandParam> mapEntrySet : parametersToReplace.entrySet()) {
				final CommandParam fdpCommandParam = mapEntrySet.getValue();
				if (mapEntrySet.getKey().equals(FDPConstant.BALANCE_OTHER_DETAILS)) {
					circleLogger.debug("Found BALANCE_OTHER_DETAILS in notification template.");
					values.put(mapEntrySet.getKey(), getBalanceOtherEnquiryvalues(fdpRequest));
				} else if (mapEntrySet.getKey().equals(FDPConstant.TARIFF_DETAILS_TEMPLATE)) {
					circleLogger.debug("Found TARIFF_DETAILS_TEMPLATE in notification template.");
					values.put(mapEntrySet.getKey(), getTariffDetailsValues(fdpRequest));
				} else if (fdpCommandParam instanceof CommandParamInput) {
					if(fdpCommandParam.getName().equals("dataAmt")) {
					}
					final CommandParamInput inputParam = (CommandParamInput) fdpCommandParam;
					inputParam.evaluateValue(fdpRequest);
					values.put(mapEntrySet.getKey(), inputParam.getValue().toString());
					circleLogger.debug("The parameter " + mapEntrySet.getKey() + " value :- "
							+ inputParam.getValue().toString());
				}
			}
			circleLogger.debug("Final parameter map prepared for notification :"+values);
		} catch (final EvaluationFailedException e) {
			circleLogger.error("The notification could not be sent", e);
			throw new NotificationFailedException("The notification could not be sent", e);
		} catch (final ExecutionFailedException e) {
			circleLogger.error("The notification could not be sent", e);
			throw new NotificationFailedException("The notification could not be sent", e);
		} catch (final IOException e) {
			circleLogger.error("The notification could not be sent", e);
			throw new NotificationFailedException("The notification could not be sent", e);
		}
		return values;
	}

	@SuppressWarnings("unchecked")
	private static String getTariffDetailsValues(final FDPRequest fdpRequest) throws NotificationFailedException,
			ExecutionFailedException, IOException {
		final StringBuffer notificationText = new StringBuffer();
		String notificationString = null;
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		final Long notificationId = FDPConstant.TARIFF_DETAILS_NOTIFICATION_ID - fdpRequest.getCircle().getCircleId();
		final Object fdpNotificationDTOObj = ApplicationConfigUtil.getMetaDataCache().getValue(
				new FDPMetaBag(fdpRequest.getCircle(), ModuleType.NOTIFICATION, notificationId));
		notificationText.append(FDPConstant.NEWLINE);
		if (fdpNotificationDTOObj instanceof FDPNotificationVO) {
			final FDPNotificationVO fdpNotificationDTO = (FDPNotificationVO) fdpNotificationDTOObj;
			final Map<String, CommandParam> parametersToReplace = fdpNotificationDTO.getParametersToReplace();
			for (final Map<TariffEnquiryNotificationOptions, String> notificationValue : (List<Map<TariffEnquiryNotificationOptions, String>>) fdpRequest
					.getAuxiliaryRequestParameter(AuxRequestParam.TARIFF_VALUES)) {
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_NAME,
						notificationValue.get(TariffEnquiryNotificationOptions.NAME));
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_VALUE,
						notificationValue.get(TariffEnquiryNotificationOptions.VALUE));
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_VALIDITY,
						notificationValue.get(TariffEnquiryNotificationOptions.VALIDITY));
				RequestUtil.putAuxiliaryValueInRequest(fdpRequest, AuxRequestParam.TARIFF_UNIT,
						notificationValue.get(TariffEnquiryNotificationOptions.UNIT));
				final Map<String, String> values = processParameters(fdpRequest, parametersToReplace, circleLogger);
				notificationText.append(processTemplate(fdpNotificationDTO.getMessage(), values, circleLogger)).append(
						FDPConstant.NEWLINE);
			}
			final String tempNotification = notificationText.substring(0,
					notificationText.lastIndexOf(FDPConstant.NEWLINE));
			notificationString = tempNotification.toString();
			notificationString = notificationString.replaceAll(FDPConstant.SPACE + FDPConstant.SPACE
					+ FDPConstant.SPACE, FDPConstant.SPACE);
			circleLogger.debug("Final Tariff Details Prepared:["+notificationString+"]");
		}
		return notificationString;
	}

	@SuppressWarnings("unchecked")
	private static boolean checkFailureReasonPresent(final FDPRequest fdpRequest) {
		boolean isCutomisedMessage=false;
		Map<String,Integer> fdpAttributeWithNotification = (Map<String,Integer>) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAILURE_REASON);
		if(null != fdpAttributeWithNotification && fdpAttributeWithNotification.size() ==1) {
			isCutomisedMessage = true;
		}
		return isCutomisedMessage;
	}
	
	@SuppressWarnings("unchecked")
	private static String failureReasonNotificationId(final FDPRequest fdpRequest, final Logger circleLogger ) throws NotificationFailedException {
		Map<String,Integer> fdpAttributeWithNotification = (Map<String,Integer>) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAILURE_REASON);
		Long notificationIdFromCache=0L;
		if(null != fdpAttributeWithNotification && fdpAttributeWithNotification.size() ==1) {
			for(String entrykey : fdpAttributeWithNotification.keySet()) {
				notificationIdFromCache = Long.valueOf(fdpAttributeWithNotification.get(entrykey).toString());
				break;
			}
		}
		Long notificationId =  notificationIdFromCache - fdpRequest.getCircle().getCircleId();
		return NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger);
	}
	
	@SuppressWarnings("unchecked")
	private static String getBalanceOtherEnquiryvalues(final FDPRequest fdpRequest) throws NotificationFailedException,
			ExecutionFailedException, IOException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		String notificationText = null;
		Long notificationId = FDPConstant.BALANCE_ENQUIRY_VALUES_NO_NOT_ID - fdpRequest.getCircle().getCircleId();
		final List<Map<TariffEnquiryNotificationOptions, String>> notificationValue = (List<Map<TariffEnquiryNotificationOptions, String>>) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TARIFF_VALUES);
		if(null != notificationValue && !notificationValue.isEmpty()) {
			notificationId = FDPConstant.BALANCE_ENQUIRY_VALUES_NOT_ID - fdpRequest.getCircle().getCircleId();
			notificationText = TariffEnquiryNotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger);
		} else {
			notificationText = NotificationUtil.createNotificationText(fdpRequest, notificationId, circleLogger); 
		}
		return notificationText;
	}
	
	/**
	 * This method check if BALANCE_OTHER_DETAILS is configured in notification.
	 * 
	 * @param parametersToReplace
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static boolean checkIfBalanceOtherDetailsConfigured(final Map<String, CommandParam> parametersToReplace) throws ExecutionFailedException {
		boolean isBalanceOtherDetailsConfigured = false;
		for (final Map.Entry<String, CommandParam> mapEntrySet : parametersToReplace.entrySet()) {
			if (mapEntrySet.getKey().equals(FDPConstant.BALANCE_OTHER_DETAILS)) {
				isBalanceOtherDetailsConfigured = true;
				break;
			}
		}
		return isBalanceOtherDetailsConfigured;
	}
}
