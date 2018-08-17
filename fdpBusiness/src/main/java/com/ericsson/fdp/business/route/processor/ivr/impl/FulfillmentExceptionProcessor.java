package com.ericsson.fdp.business.route.processor.ivr.impl;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseCodes;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

/**
 * This class handles all the exceptions occured during FDP flow and show an
 * generic messages to the IVR caller and logs the exception.
 * 
 * @author Ericsson
 */
public class FulfillmentExceptionProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentExceptionProcessor.class);

	/** The Constant METHOD_LOG_EXCEPTION. */
	private static final String METHOD_LOG_EXCEPTION = "logException";

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Throwable exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
		logException(exchange, this.getClass(), METHOD_PROCESS, exception);
		exchange.setProperty(Exchange.EXCEPTION_CAUGHT, null);
		sendResponse(exchange, FulfillmentResponseCodes.FDP_EXCEPTION);
	}

	/**
	 * Log exception.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param clazz
	 *            the clazz
	 * @param methodName
	 *            the method name
	 * @param exception
	 *            the exception
	 * @throws ExecutionFailedException
	 */
	final private void logException(final Exchange exchange, final Class<?> clazz, final String methodName,
			final Throwable exception) throws ExecutionFailedException {
		final FDPCircle fdpCircle = exchange.getIn().getHeader(FDPRouteHeaders.FDP_CIRCLE.getValue(), FDPCircle.class);
		if (fdpCircle == null) {
			LOGGER.error(exception.getMessage(), exception);
		} else {
			final ch.qos.logback.classic.Logger circleRequestLogger = getCircleRequestLogger(fdpCircle);
			FDPLogger.error(circleRequestLogger, clazz, methodName, exception.getMessage(), exception);
			printPostRequestLogs(exchange);
			logInfoMessageInRequestLogs(exchange, getClass(), METHOD_LOG_EXCEPTION, "Fulfillment Provisioning Result:"+Status.FAILURE.getStatusText());
		}
	}
}
