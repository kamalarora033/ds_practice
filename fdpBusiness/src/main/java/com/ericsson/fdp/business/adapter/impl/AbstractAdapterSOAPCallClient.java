package com.ericsson.fdp.business.adapter.impl;

import org.apache.camel.Exchange;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.slf4j.Logger;

import com.ericsson.fdp.business.adapter.AdapterSOAPCallClient;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * The Class AbstractAdapterHttpCallClient implements AdapterHttpCallClient system.
 */
public abstract class AbstractAdapterSOAPCallClient implements AdapterSOAPCallClient {

	/**
	 * Post process of logs success.
	 *
	 * @param circleLoggerRequest the circle logger request
	 * @param requestId the request id
	 */
	public void postProcessOfLogsSuccess(final Logger circleLoggerRequest, final String requestId) {
		FDPLogger.info(
				circleLoggerRequest,
				getClass(),
				"process()",
				new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(requestId).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_SUCCESS).toString());
	}
	
	/**
	 * Post process of logs failure.
	 *
	 * @param circleLoggerRequest the circle logger request
	 * @param requestId the request id
	 * @param exchange the exchange
	 */
	public void postProcessOfLogsFailure(final Logger circleLoggerRequest, final String requestId , final Exchange exchange) {
		FDPLogger.info(
				circleLoggerRequest,
				getClass(),
				"process()",
				new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(requestId).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_FAILURE)
						.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESRSN)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(getStatusText(exchange)).toString());
	}

	/**
	 * Gets the logger.
	 *
	 * @param circleName the circle name
	 * @param externalSystem the external system
	 * @return the logger
	 */
	public Logger getLogger(final String circleName, final ExternalSystem externalSystem) {
		return FDPLoggerFactory.getRequestLogger(circleName, getModuleName(externalSystem));
	}

	/**
	 * Gets the module name.
	 *
	 * @param externalSystem the external system
	 * @return the module name
	 */
	private String getModuleName(final ExternalSystem externalSystem) {
		String moduleName = null;
		switch (externalSystem) {
		case Ability:
			moduleName = BusinessModuleType.ABILITY.name();
			break;
		case ESF:
			moduleName = BusinessModuleType.ESF.name();
			break;	
		default:
			break;
		}
		return moduleName;
	}
	
	/**
	 * Gets the status text.
	 *
	 * @param exchange the exchange
	 * @return the status text
	 */
	private String getStatusText(Exchange exchange) {
		String statusText = null;
		final Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
		if (caused instanceof HttpOperationFailedException) {
			statusText = ((HttpOperationFailedException) caused).getStatusText();
		} else {
			statusText = caused.getMessage();
		}
		return statusText;
	}

}
