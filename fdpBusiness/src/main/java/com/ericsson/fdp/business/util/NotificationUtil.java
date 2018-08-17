package com.ericsson.fdp.business.util;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.constants.NotificationConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.vo.ErrorCodesNotificationVO;
import com.ericsson.fdp.business.vo.FDPNotificationVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.FDPSPNotificationParamEnum;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.dsm.framework.service.impl.FDPCircleCacheProducer;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.ResponseError;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.ExchangeMessageResponse;
import com.ericsson.fdp.dao.enums.LanguageType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/*
 Feature Name: User can purchase bundle for self and others
 Changes: Updated sendOfflineNotification to send SMS notification to beneficiary in case it is set
 Date: 30-10-2015
 Singnum Id:ESIASAN
 */

/**
 * This class is the utility class for notification creation.
 *
 * @author Ericsson
 *
 */
public class NotificationUtil {

	private static final String EXPIRY_START_TAG="<expiry>";
	private static final String EXPIRY_END_TAG="</expiry>";
	private static final String DA_START_TAG="<da>";
	private static final String DA_END_TAG="</da>";

	/**
	 * Instantiates a new notification util.
	 */
	protected NotificationUtil() {

	}

	/**
	 * This method is used to send the notification.
	 *
	 * @param fdpRequest
	 *            The request for which the notification is to be sent.
	 * @param notificationId
	 *            The id for which the notification is to be sent.
	 * @param circleLogger 
	 * @return Notification text created.
	 * @throws NotificationFailedException
	 *             Exception, if any in sending the notification.
	 */
	public static String createNotificationText(final FDPRequest fdpRequest,
			final Long notificationId, final Logger circleLogger)
					throws NotificationFailedException {
		String notificationText = null;
		try {
			circleLogger.debug(LoggerUtil.getRequestAppender(fdpRequest)
					+ "The notification id to be used " + notificationId);
			if (notificationId != null) {
				final Object fdpNotificationDTOObj = ApplicationConfigUtil
						.getMetaDataCache()
						.getValue(
								new FDPMetaBag(fdpRequest.getCircle(),
										ModuleType.NOTIFICATION, notificationId));
				if (fdpNotificationDTOObj instanceof FDPNotificationVO) {
					final FDPNotificationVO fdpNotificationDTO = (FDPNotificationVO) fdpNotificationDTOObj;
					FDPLogger.debug(circleLogger, NotificationUtil.class,
							"createNotificationText()",
							LoggerUtil.getRequestAppender(fdpRequest)
							+ "NotificationVO found "
							+ fdpNotificationDTO);
					final Map<String, CommandParam> parametersToReplace = fdpNotificationDTO
							.getParametersToReplace();
					final Map<String, String> values = processParameters(
							fdpRequest, parametersToReplace, circleLogger);
					notificationText = processTemplate(
							fdpNotificationDTO.getMessage(), values,
							circleLogger);
					FDPLogger.debug(circleLogger, NotificationUtil.class,
							"createNotificationText()",
							LoggerUtil.getRequestAppender(fdpRequest)
							+ "Notification text formed "
							+ notificationText);
					String notificationTextforLog = new String(notificationText);
					notificationTextforLog = notificationTextforLog.replace("\n", "").replace("\r", "");
					FDPLogger.info(circleLogger, NotificationUtil.class,
							"createNotificationText()",
							LoggerUtil.getRequestAppender(fdpRequest) + "NOTIF"
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER
									+ notificationTextforLog
									+ FDPConstant.LOGGER_DELIMITER + "TID"
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER
									+ fdpNotificationDTO.getName()
									+ FDPConstant.LOGGER_DELIMITER + "MSISDN"
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER
									+ fdpRequest.getSubscriberNumber()
									+ FDPConstant.LOGGER_DELIMITER + FDPLoggerConstants.CORRELATION_ID
									+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER 
									+ fdpRequest.getOriginTransactionID().toString());
				}
			}
		} catch (final ExecutionFailedException e) {
			FDPLogger.error(circleLogger, NotificationUtil.class,
					"createNotificationText()",
					LoggerUtil.getRequestAppender(fdpRequest)
					+ "Could not send notification", e);
			throw new NotificationFailedException(
					"Could not send notification", e);
		} catch (final IOException e) {
			FDPLogger.error(circleLogger, NotificationUtil.class,
					"createNotificationText()",
					LoggerUtil.getRequestAppender(fdpRequest)
					+ "Could not create notification", e);
			throw new NotificationFailedException(
					"Could not create notification", e);
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
	 * @return Map of parameter to replace and its value.
	 * @throws NotificationFailedException
	 *             Exception, if parameter value could not be evaluated.
	 */
	protected static Map<String, String> processParameters(
			final FDPRequest fdpRequest,
			final Map<String, CommandParam> parametersToReplace,
			final Logger circleLogger) throws NotificationFailedException {
		final Map<String, String> values = new HashMap<String, String>();
		for (final Map.Entry<String, CommandParam> mapEntrySet : parametersToReplace
				.entrySet()) {
			final CommandParam fdpCommandParam = mapEntrySet.getValue();
			if (fdpCommandParam instanceof CommandParamInput) {
				final CommandParamInput inputParam = (CommandParamInput) fdpCommandParam;
				try {
					inputParam.evaluateValue(fdpRequest);
					values.put(mapEntrySet.getKey(), inputParam.getValue()
							.toString());
					circleLogger.debug("The parameter " + mapEntrySet.getKey()
					+ " value :- " + inputParam.getValue().toString());
				} catch (final EvaluationFailedException e) {
					circleLogger.error("The notification could not be sent", e);
					throw new NotificationFailedException(
							"The notification could not be sent", e);
				}
			}
		}
		return values;
	}

	/**
	 * The method processes the template provided and returns the string value
	 * after processing the template.
	 *
	 * @param template
	 *            The template to process.
	 * @param values
	 *            The values of objects in the template.
	 * @return the template as a string.
	 * @throws NotificationFailedException
	 *             Exception, in case the template could not be processed.
	 */
	protected static String processTemplate(final Template template,
			final Map<String, String> values, final Logger circleLogger)
					throws NotificationFailedException {
		final Writer notificationMessageText = new StringWriter();
		try {
			template.process(values, notificationMessageText);
		} catch (final TemplateException e) {
			circleLogger
			.error("The notification could not be sent as the template is incorrect.",
					e);
			throw new NotificationFailedException(
					"The notification could not be sent as the template is incorrect.",
					e);
		} catch (final IOException e) {
			circleLogger
			.error("The notification could not be sent as the template is incorrect.",
					e);
			throw new NotificationFailedException(
					"The notification could not be sent as template could not be processed.",
					e);
		}
		return notificationMessageText.toString();
	}

	// This method can be used to test the notification text that is created.
	//
	// public static void main(String[] args) throws
	// NotificationFailedException, IOException {
	// Map<String, String> map = new HashMap<String, String>();
	// map.put("abc_def", "123213");
	// System.out.println(processTemplate(new Template("asdasd", new
	// StringReader("${abc_def} sdfd afdsf asdf"), new Configuration()), map,
	// null));
	// }

	/**
	 * This method is used to send the notification.
	 *
	 * @param msisdn
	 *            the msisdn on which the notification is to be sent.
	 * @param channelType
	 *            the channel on which the notification is to be sent.
	 * @param fdpCircle
	 *            The circle on which the notification is to be sent.
	 * @param notification
	 *            The notification to be sent.
	 * @return True if notification was successfully sent, false other wise.
	 * @throws NotificationFailedException
	 *             Exception, if any in sending the notification.
	 */
	public static Boolean sendNotification(final Long msisdn,
			final ChannelType channel, final FDPCircle fdpCircle,
			final String notification, final String requestId,
			final boolean addDelay) throws NotificationFailedException {
		final ExchangeMessageResponse message = createExchangeMessage(msisdn,
				channel, fdpCircle, notification, requestId, addDelay);
		final Logger logger = FDPLoggerFactory
				.getRequestLogger(fdpCircle.getCircleName(),
						BusinessModuleType.SMSC_SOUTH.name());
		logger.debug("The notification is being sent to " + msisdn
				+ " for circle " + fdpCircle + " notification " + notification);
		try {
			final FDPCircleCacheProducer circleCacheProducer = ApplicationConfigUtil
					.getFDPCircleCacheProducer();
			message.setIncomingTrxIpPort(Inet4Address.getLocalHost()
					.getHostAddress());
			circleCacheProducer.pushToQueue(message, fdpCircle.getCircleCode(),
					ApplicationConfigUtil.getApplicationConfigCache());
		} catch (final UnknownHostException e) {
			throw new NotificationFailedException(
					"Could not send notification as host not found", e);
		} catch (final ExecutionFailedException e) {
			throw new NotificationFailedException(
					"Could not send notification as execution failed", e);
		}
		logger.debug("The notification has been sent");
		return true;
	}

	/**
	 * This message is used to create the exchange which is sent to the user.
	 *
	 * @param msisdn
	 *            the msisdn to be used.
	 * @param channel
	 *            the channel on which the notification is sent.
	 * @param fdpCircle
	 *            the circle for the subscriber.
	 * @param notification
	 *            the notification text to be sent.
	 * @param requestId
	 *            the request id to be used.
	 * @param addDelay
	 *            true, if delay is to be added, false for zero delay.
	 * @return the exchange message created.
	 */
	private static ExchangeMessageResponse createExchangeMessage(
			final Long msisdn, final ChannelType channel,
			final FDPCircle fdpCircle, final String notification,
			final String requestId, final boolean addDelay) {
		final ExchangeMessageResponse message = new ExchangeMessageResponse();
		message.setExternalSystemType(channel.getName());
		message.setMsisdn(msisdn.toString());
		message.setServiceModeType("WAP");
		message.setBody(notification);
		message.setRequestId(requestId);
		message.setCircleId(fdpCircle.getCircleCode());
		if (addDelay) {
			updateDelayInMessage(fdpCircle, message);
		}
		return message;
	}

	/**
	 * This method is used to update the delay in message.
	 *
	 * @param fdpCircle
	 *            the circle for which the delay is to be added.
	 * @param message
	 *            the message in which the delay is to be added.
	 */
	private static void updateDelayInMessage(final FDPCircle fdpCircle,
			final ExchangeMessageResponse message) {
		final Object delayTime = fdpCircle.getConfigurationKeyValueMap().get(
				ConfigurationKey.SMS_DELAY_TIME.getAttributeName());
		if (delayTime != null) {
			try {
				message.setDelayTime(Long.valueOf(delayTime.toString()));
			} catch (final NumberFormatException e) {
				final Logger logger = FDPLoggerFactory.getRequestLogger(
						fdpCircle.getCircleName(),
						BusinessModuleType.SMSC_SOUTH.name());
				logger.error("The configuration key is not defined properly ",
						e);
			}
		}
	}

	/**
	 * This method is used to send an offline notification, in case of message
	 * to be sent on successful buy of a product.
	 *
	 * @param fdpRequest
	 *            the request to be used.
	 * @param notificationText
	 *            the text to be used.
	 */
	public static void sendOfflineNotification(final FDPRequest fdpRequest,
			final String notificationText) {
		sendOfflineNotification(fdpRequest, notificationText, false);
	}

	/**
	 * This method is used to send an offline notification, in case of message
	 * to be sent on successful buy of a product.
	 *
	 * @param fdpRequest
	 *            the request to be used.
	 * @param notificationText
	 *            the text to be used.
	 * @param skipChannelCheck
	 *            skip check for channel to send the notification text.
	 */
	public static void sendOfflineNotification(final FDPRequest fdpRequest,
			final String notificationText, final boolean skipChannelCheck) {

		final FDPCacheable fdpCacheable = fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		String isSendSms = "true";
		if (fdpCacheable instanceof Product) {
			final Product product = (Product) fdpCacheable;
			if (null != (product
					.getAdditionalInfo(ProductAdditionalInfoEnum.SEND_TO_SMS))) {
				if (!(product
						.getAdditionalInfo(ProductAdditionalInfoEnum.SEND_TO_SMS)
						.isEmpty())) {
					isSendSms = product
							.getAdditionalInfo(ProductAdditionalInfoEnum.SEND_TO_SMS);
				}
			}
		}

		if (null != (fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.SEND_SMS))) {
			isSendSms = (String) fdpRequest
					.getAuxiliaryRequestParameter(AuxRequestParam.SEND_SMS);
		}

		if (("true").equals(isSendSms)) {
			if(!(NotificationConstants.NOTIFICATION_PRODUCT_BUY_CHANNELS.contains(fdpRequest.getChannel()) && skipChannelCheck)){			
				final Runnable runnable = new Runnable() {
					//  }
					@Override
					public void run() {
                        try {

                            if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ORIGINAL_MSISDN) != null) {
                                sendNotification(
                                        Long.parseLong(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ORIGINAL_MSISDN).toString()),
                                        ChannelType.SMS,
                                        fdpRequest.getCircle(),
                                        notificationText, fdpRequest.getRequestId(), true);
                            } else {
                                sendNotification(fdpRequest.getSubscriberNumber(), ChannelType.SMS, fdpRequest.getCircle(),
                                        notificationText, fdpRequest.getRequestId(), true);

                            }
                            // Sending SMS notification to beneficiary, in case
                            // beneficiary is set
                            Object beneficiaryMsisdnObject = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN);
                            if (null != beneficiaryMsisdnObject) {
                                Long beneficiaryMsisdn = Long.valueOf(beneficiaryMsisdnObject.toString());
                                Object beneficiaryNotificationObject = fdpRequest
                                        .getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_NOTIFICATION);
                                if (null != beneficiaryNotificationObject && beneficiaryNotificationObject instanceof String) {
                                    String beneficiaryNotificationText = beneficiaryNotificationObject.toString();
                                    NotificationUtil.sendNotification(beneficiaryMsisdn, ChannelType.SMS, fdpRequest.getCircle(),
                                            beneficiaryNotificationText, fdpRequest.getRequestId(), true);
                                }
                            }
                            if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.isData2Share)
                                    || null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IS_TIME2SHARE)) {
                                Object receiverNotificationObject = fdpRequest
                                        .getAuxiliaryRequestParameter(AuxRequestParam.RECEIVER_NOTIFICATION);
                                if (null != receiverNotificationObject) {
                                    NotificationUtil.sendNotification(
                                            Long.valueOf(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT)
                                                    + ""), ChannelType.SMS, fdpRequest.getCircle(), receiverNotificationObject.toString(),
                                            fdpRequest.getRequestId(), true);
                                }
                            }
                        } catch (final NotificationFailedException e) {
							final Logger logger = LoggerUtil
									.getSummaryLoggerFromRequest(fdpRequest);
							FDPLogger.error(logger, this.getClass(),
									"sendOfflineNotification()",
									LoggerUtil.getRequestAppender(fdpRequest)
									+ "Notification could not be sent", e);
						}
					}

				};
				final Thread thread = new Thread(runnable);
				thread.start();
			}
		}
	}
	/**
	 * Gets the notification id for command.
	 *
	 * @param fdpcircle
	 *            the fdpcircle
	 * @param lastExecutedCommand
	 *            the last executed command
	 * @return the notification id for command
	 * @throws ExecutionFailedException
	 */
	public static Long getNotificationIdForCommand(final FDPCircle fdpcircle,
			final FDPCommand lastExecutedCommand)
					throws ExecutionFailedException {
		ResponseError responseError = lastExecutedCommand.getResponseError();
		Long notificationId = null;
		ErrorCodesNotificationVO errorCodeNotificationVO = null;
		FDPCache<FDPMetaBag, FDPCacheable> fdpCache = ApplicationConfigUtil
				.getMetaDataCache();
		if (ErrorTypes.FAULT_CODE.name().equals(responseError.getErrorType())) {
			/*String key = lastExecutedCommand.getSystem()
					+ FDPConstant.PARAMETER_SEPARATOR
					+ responseError.getResponseCode();*/
			String key = lastExecutedCommand.getCommandDisplayName()
					+ FDPConstant.PARAMETER_SEPARATOR
					+ responseError.getResponseCode();
			FDPMetaBag metaBag = new FDPMetaBag(fdpcircle,
					ModuleType.FAULT_CODE_NOTIFICATION_MAPPING, key);

			FDPMetaBag metaBag1 = new FDPMetaBag(fdpcircle,
					ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING, key);

			errorCodeNotificationVO = (ErrorCodesNotificationVO) fdpCache
					.getValue(metaBag);

			if (errorCodeNotificationVO == null) {
				errorCodeNotificationVO = (ErrorCodesNotificationVO) fdpCache
						.getValue(metaBag1);
			}

		} else if (ErrorTypes.RESPONSE_CODE.name().equals(
				responseError.getErrorType())) {
			String key = lastExecutedCommand.getCommandDisplayName()
					+ FDPConstant.PARAMETER_SEPARATOR
					+ responseError.getResponseCode();
			FDPMetaBag metaBag = new FDPMetaBag(fdpcircle,
					ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING, key);
			errorCodeNotificationVO = (ErrorCodesNotificationVO) fdpCache
					.getValue(metaBag);
		} // In case of unknown error type, pass the null for notificationId

		if (errorCodeNotificationVO != null) {
			notificationId = errorCodeNotificationVO.getNotificationId();
		}
		return notificationId;
	}

	/**
	 * 
	 * @param fdpRequest
	 * @param notificationId
	 * @param circleLogger
	 * @return
	 * @throws NotificationFailedException
	 */
	public static String createNotificationTextForTariff(
			final FDPRequest fdpRequest, final Long notificationId,
			final Logger circleLogger) throws NotificationFailedException {
		String notificationText = null;
		try {
			circleLogger.debug(LoggerUtil.getRequestAppender(fdpRequest)
					+ "The notification id to be used " + notificationId);
			if (notificationId != null) {
				final Object fdpNotificationDTOObj = ApplicationConfigUtil
						.getMetaDataCache()
						.getValue(
								new FDPMetaBag(fdpRequest.getCircle(),
										ModuleType.NOTIFICATION, notificationId));
				if (fdpNotificationDTOObj instanceof FDPNotificationVO) {
					final FDPNotificationVO fdpNotificationDTO = (FDPNotificationVO) fdpNotificationDTOObj;
					FDPLogger.debug(circleLogger, NotificationUtil.class,
							"createNotificationText()",
							LoggerUtil.getRequestAppender(fdpRequest)
							+ "NotificationVO found "
							+ fdpNotificationDTO);
					final Map<String, CommandParam> parametersToReplace = fdpNotificationDTO
							.getParametersToReplace();
					final Map<String, String> values = processParameters(
							fdpRequest, parametersToReplace, circleLogger);
					notificationText = processTemplate(
							fdpNotificationDTO.getMessage(), values,
							circleLogger);
					FDPLogger.debug(circleLogger, NotificationUtil.class,
							"createNotificationText()",
							LoggerUtil.getRequestAppender(fdpRequest)
							+ "Notification text formed "
							+ notificationText);
					/*
					 * FDPLogger.info(circleLogger, NotificationUtil.class,
					 * "createNotificationText()",
					 * LoggerUtil.getRequestAppender(fdpRequest) + "NOTIF" +
					 * FDPConstant.LOGGER_KEY_VALUE_DELIMITER + notificationText
					 * + FDPConstant.LOGGER_DELIMITER + "TID" +
					 * FDPConstant.LOGGER_KEY_VALUE_DELIMITER +
					 * fdpNotificationDTO.getName()+FDPConstant.LOGGER_DELIMITER
					 * +"MSISDN"+FDPConstant.LOGGER_KEY_VALUE_DELIMITER +
					 * fdpRequest.getSubscriberNumber());
					 */
				}
			}
		} catch (final ExecutionFailedException e) {
			FDPLogger.error(circleLogger, NotificationUtil.class,
					"createNotificationText()",
					LoggerUtil.getRequestAppender(fdpRequest)
					+ "Could not send notification", e);
			throw new NotificationFailedException(
					"Could not send notification", e);
		} catch (final IOException e) {
			FDPLogger.error(circleLogger, NotificationUtil.class,
					"createNotificationText()",
					LoggerUtil.getRequestAppender(fdpRequest)
					+ "Could not create notification", e);
			throw new NotificationFailedException(
					"Could not create notification", e);
		}
		return notificationText;
	}

	protected static FDPNotificationVO getFDPNotificationVOForOtherLanguage(final FDPNotificationVO fdpNotificationVO, final FDPRequest fdpRequest) {
		FDPNotificationVO notificationVO = fdpNotificationVO;
		if(null != fdpNotificationVO && null != fdpRequest.getSimLangauge() && !LanguageType.ENGLISH.equals(fdpRequest.getSimLangauge()) && null != fdpNotificationVO.getOtherLangNotificationMap() && !fdpNotificationVO.getOtherLangNotificationMap().isEmpty()) {
			notificationVO = fdpNotificationVO.getOtherLangNotificationMap().get(fdpRequest.getSimLangauge());
		}
		return notificationVO;
	}


	/**
	 * 
	 * @param fdpRequest
	 * @param notificationText
	 * @param params
	 * @return the modifiedNotificationText
	 * @throws NotificationFailedException
	 */
	public static String getModifiedNotificationText(FDPRequest fdpRequest , String notificationText , FDPSPNotificationParamEnum params) throws NotificationFailedException
	{
		String modifiedNotificationText = null;
		if(notificationText!=null)
		{
            String dateFormat = fdpRequest.getCircle().getConfigurationKeyValueMap()
                    .get(ConfigurationKey.BALANCE_CHECK_DATE_FORMAT.getAttributeName());

            if (dateFormat == null || dateFormat.isEmpty())
                dateFormat = FDPConstant.DATE_PATTERN;
		    
			switch(params)
			{
			case EXPIRY_DATE_ASC_ORDER:
                modifiedNotificationText = getExpirySortedText(notificationText, true, dateFormat);
				break;
			case DA_ID:
				modifiedNotificationText = omitZeroDA(notificationText);
				break;			
			case ALL:
                modifiedNotificationText = getAllModifiedText(notificationText, true, dateFormat);
				break;
			default :
				break;
			}
			modifiedNotificationText = overrideNullNotification(fdpRequest, modifiedNotificationText);
		}
		return modifiedNotificationText;
	}

	/**
	 * 
	 * @param notificationText
	 * @param ascOrder	 
	 * @return the modifiedText
	 * @throws NotificationFailedException
	 */
    private static String getExpirySortedText(String notificationText, boolean ascOrder, String dateFormat)
            throws NotificationFailedException
	{

		Map<Date,List<String>> textMap = new TreeMap<>();
		if(!ascOrder)
			textMap = new TreeMap<>(Collections.reverseOrder());

		StringBuilder builder = new StringBuilder();
        Matcher dateMatcher;
        int counter = 0;
		for(String lines : notificationText.split("\n"))
		{
			dateMatcher = Pattern.compile(EXPIRY_START_TAG+"(.+?)"+EXPIRY_END_TAG).matcher(lines);
			if(dateMatcher.find())
			{
                
				try {
                    Date date = new SimpleDateFormat(dateFormat).parse(dateMatcher.group(1));
                   /* builder.append(DA_START_TAG + EXPIRY_START_TAG + (++counter));
                    builder.append("\n");*/
					if(textMap.containsKey(date))
					{
						textMap.get(date).add(lines.replaceAll(EXPIRY_START_TAG+"|"+EXPIRY_END_TAG, ""));
					}
					else
					{
						List<String> list = new ArrayList<>();
						list.add(lines.replaceAll(EXPIRY_START_TAG+"|"+EXPIRY_END_TAG, ""));
						textMap.put(date,list);
					}

				} catch (ParseException e) {									
					throw new NotificationFailedException("Unable to parse Expiry date ", e);
				}
			}
			else
			{
				lines = lines.replaceAll(EXPIRY_START_TAG+"|"+EXPIRY_END_TAG, "");
				builder.append(lines);
                builder.append("\n");
			}
		}
        //int replacer = 0;
		String nonExpiryText = removeEndLine(builder.toString());
		StringBuilder expiryTagData = new StringBuilder();
		for(Entry<Date,List<String>> entry : textMap.entrySet())		
			for(String text : entry.getValue())			
                expiryTagData.append(text).append("\n");
		expiryTagData.append(nonExpiryText); 
		
        return removeEndLine(expiryTagData.toString());
	}

	/**
	 * 
	 * @param notificationText	 
	 * @return the modifiedText
	 * @throws NotificationFailedException
	 */
	private  static String omitZeroDA(String notificationText) throws NotificationFailedException
	{
		StringBuilder builder = new StringBuilder();
		Matcher daMatcher ;		
		boolean flag=false;
		for(String lines : notificationText.split("\n"))
		{
			daMatcher = Pattern.compile(DA_START_TAG+"(.+?)"+DA_END_TAG+"|"+DA_START_TAG+DA_END_TAG).matcher(lines);			
			if(daMatcher.find())
			{//&& daMatcher.group(1).trim().startsWith("0")
				Boolean bolOmitLine = lines.contains(FDPConstant.STR_EMPTY_DA);
				 if( null!=daMatcher.group(1) &&!daMatcher.group(1).trim().isEmpty() && !bolOmitLine && !daMatcher.group(1).trim().startsWith("0") )
				{
					builder.append(lines.replaceAll(DA_START_TAG+"|"+DA_END_TAG, ""));
					builder.append("\n");
					flag=true;
				}
				 else if (null!=daMatcher.group(1) &&!daMatcher.group(1).trim().isEmpty() && !bolOmitLine && daMatcher.group(1).trim().startsWith("0.")) {
					 builder.append(lines.replaceAll(DA_START_TAG+"|"+DA_END_TAG, ""));
						builder.append("\n");
						flag=true;
				 }
			}

			else
			{
				lines = lines.replaceAll(DA_START_TAG+"|"+DA_END_TAG, "");
				builder.append(lines);
				builder.append("\n");
			}
		}
		if(!flag){
			return new String();
		}
		
		return removeEndLine(builder.toString());
	}

	/**
	 * 
	 * @param notificationText
	 * @param ascOrder
	 * @return the modifiedText
	 * @throws NotificationFailedException
	 */
    private static String getAllModifiedText(String notificationText, boolean ascOrder, String dateFormat)
            throws NotificationFailedException
	{
		String modifiedText = omitZeroDA(notificationText);
		if(!modifiedText.isEmpty())
		{
			modifiedText = getExpirySortedText(modifiedText, ascOrder, dateFormat);
		}
		return modifiedText;
	}

	/**
	 * 
	 * @param fdpRequest
	 * @param notificationText
	 * @return the modified text
	 */
	private static String overrideNullNotification(FDPRequest fdpRequest , String notificationText)
	{
		String modifiedText = notificationText;
		if(notificationText==null || notificationText.isEmpty())
		{
			String defaultNotification = RequestUtil.getConfigurationKeyValue(fdpRequest, ConfigurationKey.DEFAULT_NOTIFICATION);
			if(null!=defaultNotification)
				modifiedText = defaultNotification;
		}
		return modifiedText;
	}

	/**
	 * This method remove extra new line from end 
	 * @param text
	 * @return the Text
	 */
	private static String removeEndLine(String text){
		if(text!=null && !text.isEmpty() && text.endsWith("\n"))
			return removeEndLine(text.substring(0, text.lastIndexOf('\n')));
		return text.trim();
	}
}
