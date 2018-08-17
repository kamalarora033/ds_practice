package com.ericsson.fdp.business.notification;

import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The interface corresponding to the notification. This interface provides the
 * method to send notification for service provisioning.
 *
 * @author Ericsson
 *
 */
public interface FDPServiceProvisioningNotification {

	/**
	 * This method is used to send the notification.
	 *
	 * @param fdpRequest
	 *            The request for which the notification is to be sent.
	 * @param status
	 *            The status for which the notification is to be sent.
	 * @return True if notification was successfully sent, false other wise.
	 * @throws NotificationFailedException
	 *             Exception, if any in sending the notification.
	 */
	String createNotificationText(FDPRequest fdpRequest, Status status) throws NotificationFailedException;

	/**
	 * Creates the notification text.
	 *
	 * @param fdpRequest the fdp request
	 * @param executeStatus the execute status
	 * @param b the b
	 * @return the string
	 * @throws NotificationFailedException the notification failed exception
	 */
	String createNotificationText(FDPRequest fdpRequest, Status executeStatus,
			boolean checkChannel) throws NotificationFailedException;

	/**
	 * Creates the notification text after Async command execution.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws NotificationFailedException
	 */
	String createNotificationForAsyncSuccess(final FDPRequest fdpRequest) throws NotificationFailedException;
}
