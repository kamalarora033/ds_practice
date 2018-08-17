package com.ericsson.fdp.business.util;

import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.request.impl.FDPStepResponseImpl;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.enums.StepNameEnum;

/**
 * This class is the utility class for pending request.
 * 
 * @author Ericsson
 * 
 */
public class PendingRequestUtil {

	/**
	 * Instantiates a new pending request util.
	 */
	private PendingRequestUtil() {

	}

	/**
	 * This method is used to create the notification text.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @param senderMsisdn
	 *            the sender msisdn
	 * @param accReqNumber
	 *            the account request number
	 * @return the notification created.
	 * @throws NotificationFailedException
	 *             Exception, if any
	 */
	public static String createNotificationText(final FDPRequest fdpRequest, final Long senderMsisdn,
			final Long accReqNumber) throws NotificationFailedException {
		FDPStepResponseImpl stepResponse = new FDPStepResponseImpl();
		stepResponse.addStepResponseValue(SharedAccountResponseType.PROVIDER_MSISDN.name(), senderMsisdn);
		RequestUtil.putStepResponseInRequest(stepResponse, fdpRequest, StepNameEnum.VALIDATION_STEP.getValue());
		stepResponse = new FDPStepResponseImpl();
		stepResponse.addStepResponseValue(SharedAccountResponseType.ACC_REQ_NO.name(), accReqNumber);
		RequestUtil.putStepResponseInRequest(stepResponse, fdpRequest, StepNameEnum.DATABASE_STEP.getValue());
		return NotificationUtil.createNotificationText(fdpRequest, FDPConstant.PENDING_REQUEST_NOT_ID,
				LoggerUtil.getSummaryLoggerFromRequest(fdpRequest));
	}

}
