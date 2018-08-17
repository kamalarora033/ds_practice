package com.ericsson.fdp.business.adapter.impl;


import java.util.Calendar;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.component.http.HttpOperationFailedException;
import org.slf4j.Logger;

import com.ericsson.fdp.business.adapter.AdapterHttpCallClient;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * The Class AbstractAdapterHttpCallClient implements AdapterHttpCallClient system.
 */
public abstract class AbstractAdapterHttpCallClient implements AdapterHttpCallClient {

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
     * Post process of logs success.
     *
     * @param circleLoggerRequest the circle logger request
     * @param requestId the request id
     */
     public void postProcessOfLogsSuccess(final Logger circleLoggerRequest, final String requestId, final String exRid) {
            FDPLogger.info(
                         circleLoggerRequest,
                         getClass(),
                         "process()",
                         new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
                                       .append(requestId).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
                                       .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_SUCCESS)
                                       .append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.REPORTTYPE)
                                       .append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
                                       .append(FDPConstant.RESPEXTREPORT).append(FDPConstant.LOGGER_DELIMITER)
                                       .append("EXRID").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
                                       .append(exRid).toString());
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
		case AIR:
			moduleName = BusinessModuleType.AIR_SOUTH.name();
			break;
		case CGW:
			moduleName = BusinessModuleType.CGW_SOUTH.name();
			break;
		case RS:
			moduleName = BusinessModuleType.RS_SOUTH.name();
			break;
		case MCARBON:
			moduleName = BusinessModuleType.VAS_SOUTH.name();
			break;
		case MANHATTAN:
			moduleName = BusinessModuleType.VAS_SOUTH.name();
			break;
		case CMS:
			moduleName = BusinessModuleType.CMS_SOUTH.name();
			break;
		case MCLOAN:
			moduleName = BusinessModuleType.MCLOAN_SOUTH.name();
			break;
		case FDPOFFLINE:
			moduleName = BusinessModuleType.FDPOFFLINE_SOUTH.name();
			break;
		case Loyalty:
			moduleName = BusinessModuleType.Loyalty.name();
			break;
		case MM:
			moduleName = BusinessModuleType.MM.name();
			break;
		case EVDS:
			moduleName = BusinessModuleType.EVDS.name();
			break;
		case DMC:
			moduleName = BusinessModuleType.DMC.name();
			break;
		case SBBB:
			moduleName = BusinessModuleType.VAS_SOUTH.name();
			break;
		case ADC:
			moduleName = BusinessModuleType.ADC.name();
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
		if(caused!=null){
		if (caused instanceof HttpOperationFailedException) {
			statusText = ((HttpOperationFailedException) caused).getStatusText();
		} else {
			statusText = caused.getMessage();
		}
		}
		//if clause is added for to avoid error due to implementation of Https Implementation
		else{
			statusText = "Connection refused: connect";
		}
		return statusText;
	}
	
	protected void notRespondingLogger(final ExternalSystem externalSystem, final Endpoint endpoint ) {
		System.err.println(Calendar.getInstance().getTime()+"#"+externalSystem+"#"+endpoint+"#IS DOWN.");
	}
}
