package com.ericsson.fdp.business.menu.externalservice.impl;

import javax.ejb.Stateless;

import com.ericsson.fdp.business.menu.externalservice.AbstractFDPDynamicMenuExecutorService;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.route.constant.RoutingConstant;

/**
 * This class is sued to implement the dynamic menu executor service for
 * MCarbon.
 * 
 * @author Ericsson
 * 
 */
@Stateless
public class MCarbonDynamicMenuExecutorServiceImpl extends AbstractFDPDynamicMenuExecutorService {

	/**
	 * Pre processing of logs.
	 * 
	 * @param fdpsmppRequestImpl
	 *            the fdpsmpp request impl
	 * @param incomingIP
	 *            the incoming ip
	 */
	public void preProcessingOfLogs(final FDPSMPPRequestImpl fdpsmppRequestImpl, final String incomingIP) {
		final String logMethodName = "preProcessingOfLogs()";
		final StringBuilder appenderValue = new StringBuilder();
		appenderValue.append(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpsmppRequestImpl.getRequestId()).append(FDPConstant.LOGGER_DELIMITER)
				.append(FDPConstant.INCOMING_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(incomingIP)
				.append(FDPConstant.LOGGER_DELIMITER).append(RoutingConstant.LOGICAL_NAME)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(ExternalSystem.MCARBON.name())
				.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHANNEL_TYPE)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(ExternalSystem.MCARBON.name())
				.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.MSISDN)
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(fdpsmppRequestImpl.getSubscriberNumber());
		FDPLogger.info(getCircleLogger(fdpsmppRequestImpl), getClass(), logMethodName, appenderValue.toString());

		final StringBuilder ridAndSid = new StringBuilder();
		ridAndSid.append(FDPConstant.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpsmppRequestImpl.getRequestId()).append(FDPConstant.LOGGER_DELIMITER)
				.append(FDPConstant.SESSION_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(fdpsmppRequestImpl.getRequestId());

		FDPLogger.info(getCircleLogger(fdpsmppRequestImpl), getClass(), logMethodName, ridAndSid.toString());
	}
}
