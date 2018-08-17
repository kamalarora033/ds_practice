package com.ericsson.fdp.business.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.vo.FDPDisplayTokenVO;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is the utility class for notification creation.
 * 
 * @author Ericsson
 * 
 */
public class DisplayTokenUtil {

	/**
	 * Instantiates a new notification util.
	 */
	protected DisplayTokenUtil() {

	}

	public static String createNotificationText(String input, FDPDisplayTokenVO fdpTariffUnitVO, FDPRequest fdpRequest)
			throws NotificationFailedException {
		String notificationText = null;
		try {
			final Map<String, String> values = processParameters(input, fdpRequest);
			notificationText = NotificationUtil.processTemplate(fdpTariffUnitVO.getMessage(), values,
					LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));

		} catch (final IOException e) {
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
	 * @param circleLogger
	 *            the logger
	 * @throws NotificationFailedException
	 *             Exception, if parameter value could not be evaluated.
	 * @return values map of parameter with values
	 */
	protected static Map<String, String> processParameters(String input, FDPRequest fdpRequest) throws NotificationFailedException {
		final Map<String, String> values = new HashMap<String, String>();
		values.put("value", input);
		return values;
	}

}
