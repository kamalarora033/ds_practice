package com.ericsson.fdp.business.notification;

import java.io.Serializable;

import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.vo.FDPCircle;

/**
 * The interface corresponding to the notification. This interface provides the
 * method to send notification.
 * 
 * @author Ericsson
 * 
 */
public interface FDPNotification extends Serializable {

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
	Boolean sendNotification(Long msisdn, ChannelType channelType, FDPCircle fdpCircle, String notification,
			String requestID) throws NotificationFailedException;

}
