package com.ericsson.fdp.business.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.ericsson.fdp.business.convertor.displayConvertor.UnitDisplayFormat;
import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.vo.FDPTariffUnitVO;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is the utility class for notification creation.
 * 
 * @author Ericsson
 * 
 */
public class TariffEnquiryUnitNotificationUtil {

	/**
	 * Instantiates a new notification util.
	 */
	protected TariffEnquiryUnitNotificationUtil() {

	}

	public static String createNotificationText(String input, FDPTariffUnitVO fdpTariffUnitVO, FDPRequest fdpRequest)
			throws NotificationFailedException {
		String notificationText = null;
		try {
			final Map<String, UnitDisplayFormat> parametersToReplace = fdpTariffUnitVO.getParametersToReplace();
			final Map<String, String> values = processParameters(parametersToReplace, input, fdpRequest);
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
	protected static Map<String, String> processParameters(final Map<String, UnitDisplayFormat> parametersToReplace,
			String input, FDPRequest fdpRequest) throws NotificationFailedException {
		final Map<String, String> values = new HashMap<String, String>();
		try {
			for (final Map.Entry<String, UnitDisplayFormat> mapEntrySet : parametersToReplace.entrySet()) {
				final UnitDisplayFormat fdpCommandParam = mapEntrySet.getValue();
				values.put(mapEntrySet.getKey(), fdpCommandParam.evaluateValue(input, fdpRequest));
			}
		} catch (final EvaluationFailedException e) {
			throw new NotificationFailedException("The notification could not be sent", e);
		}
		return values;
	}

}
