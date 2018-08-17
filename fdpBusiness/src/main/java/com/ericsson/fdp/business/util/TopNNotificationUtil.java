package com.ericsson.fdp.business.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.batchjob.sharedAccount.UsageObject;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.vo.FDPNotificationVO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class is the utility class for notification creation.
 * 
 * @author Ericsson
 * 
 */
public class TopNNotificationUtil extends NotificationUtil {

	/**
	 * Instantiates a new top n notification util.
	 */
	private TopNNotificationUtil() {

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
			final Logger circleLogger, final List<UsageObject> usageValues) throws NotificationFailedException {
		String notificationText = null;
		try {
			circleLogger.debug("The notification id to be used " + notificationId);
			if (notificationId != null) {
				final Object fdpNotificationDTOObj = ApplicationConfigUtil.getMetaDataCache().getValue(
						new FDPMetaBag(fdpRequest.getCircle(), ModuleType.NOTIFICATION, notificationId));
				if (fdpNotificationDTOObj instanceof FDPNotificationVO) {
					final FDPNotificationVO fdpNotificationDTO = (FDPNotificationVO) fdpNotificationDTOObj;
					FDPLogger.debug(circleLogger, NotificationUtil.class, "createNotificationText()",
							"NotificationVO found " + fdpNotificationDTO);
					final Map<String, CommandParam> parametersToReplace = fdpNotificationDTO.getParametersToReplace();
					final Map<String, String> values = processParameters(fdpRequest, parametersToReplace, circleLogger,
							usageValues);
					notificationText = NotificationUtil.processTemplate(fdpNotificationDTO.getMessage(), values,
							circleLogger);
					FDPLogger.debug(circleLogger, NotificationUtil.class, "createNotificationText()",
							"Notification text formed " + notificationText);
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
					"Could not send notification", e);
			throw new NotificationFailedException("Could not send notification", e);
		} catch (final IOException e) {
			FDPLogger.error(circleLogger, NotificationUtil.class, "createNotificationText()",
					"Could not create notification", e);
			throw new NotificationFailedException("Could not create notification", e);
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
	private static Map<String, String> processParameters(final FDPRequest fdpRequest,
			final Map<String, CommandParam> parametersToReplace, final Logger circleLogger,
			final List<UsageObject> usageValues) throws NotificationFailedException {
		final Map<String, String> values = new HashMap<String, String>();
		for (final Map.Entry<String, CommandParam> mapEntrySet : parametersToReplace.entrySet()) {
			final CommandParam fdpCommandParam = mapEntrySet.getValue();
			if (FDPConstant.TOPNUSAGE_NOT_TEXT.equals(fdpCommandParam.getName())) {
				final StringBuilder usageVals = new StringBuilder();
				for (final UsageObject usageObject : usageValues) {
					usageVals.append(usageObject.getNotificationString());
				}
				values.put(mapEntrySet.getKey(), usageVals.toString());
			} else {
				if (fdpCommandParam instanceof CommandParamInput) {
					final CommandParamInput inputParam = (CommandParamInput) fdpCommandParam;
					try {
						inputParam.evaluateValue(fdpRequest);
						values.put(mapEntrySet.getKey(), inputParam.getValue().toString());
						circleLogger.debug("The parameter " + mapEntrySet.getKey() + " value :- "
								+ inputParam.getValue());
					} catch (final EvaluationFailedException e) {
						circleLogger.error("The notification could not be sent", e);
						throw new NotificationFailedException("The notification could not be sent", e);
					}
				}
			}
		}
		return values;
	}

}
