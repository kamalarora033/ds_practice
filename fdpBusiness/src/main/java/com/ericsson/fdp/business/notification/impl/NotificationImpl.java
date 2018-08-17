package com.ericsson.fdp.business.notification.impl;

import javax.annotation.Resource;

import com.ericsson.fdp.business.exception.NotificationFailedException;
import com.ericsson.fdp.business.notification.FDPNotification;
import com.ericsson.fdp.business.util.NotificationUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.impl.FDPCircleCacheProducer;

/**
 * This class implements the notification interface. This class provides the
 * mechanism for sending the notification.
 * 
 * @author Ericsson
 * 
 */
public class NotificationImpl implements FDPNotification {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4662702399547674411L;

	@Resource(lookup = "java:global/fdpBusiness-ear/fdpCoreServices-1.0/FDPCircleCacheProducer")
	private FDPCircleCacheProducer circleCacheProducer;

	@Override
	public Boolean sendNotification(final Long msisdn, final ChannelType channelType, final FDPCircle fdpCircle,
			final String notification, String requestId) throws NotificationFailedException {
		return NotificationUtil.sendNotification(msisdn, channelType, fdpCircle, notification, requestId, false);
	}

}
