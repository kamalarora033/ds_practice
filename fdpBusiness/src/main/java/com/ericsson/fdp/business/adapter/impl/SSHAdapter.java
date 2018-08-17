package com.ericsson.fdp.business.adapter.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.direct.DirectEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.adapter.Adapter;
import com.ericsson.fdp.business.bean.SSHAdapterRequest;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.BusinessModuleType;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * This class implements the adapter for http interface.
 * 
 * @author Ericsson
 * 
 * @param <T>
 *            The parameter type which the adapter uses.
 */
public class SSHAdapter<T> implements Adapter {

	/**
	 * The fdp request, which is used to connect to appropriate external system
	 * for that circle.
	 */
	private final SSHAdapterRequest adapterRequest;

	/**
	 * The external system to be used.
	 */
	private final ExternalSystem externalSystem;

	/**
	 * The request to be executed.
	 */
	private final T httpRequest;

	/** The context. */
	private CdiCamelContext context;

	private static final Logger LOGGER = LoggerFactory.getLogger(SSHAdapter.class);

	private final FDPRequest fdpRequest;

	/**
	 * The default constructor.
	 * 
	 * @param circleCodeToSet
	 *            The circle code to set.
	 * @param externalSystem
	 *            The external system to set.
	 * @param httpRequestToSet
	 *            The http request to set.
	 */
	public SSHAdapter(final SSHAdapterRequest adapterRequest, final ExternalSystem externalSystem,
			final T httpRequestToSet, final FDPRequest fdpRequest) {
		this.adapterRequest = adapterRequest;
		this.externalSystem = externalSystem;
		this.httpRequest = httpRequestToSet;
		this.fdpRequest = fdpRequest;
	}

	@Override
	public Map<String, Object> callClient() throws ExecutionFailedException {
		final Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			LOGGER.debug("Invoking callClient().... with adapter request as " + adapterRequest);
			LOGGER.debug("Adapter request endpoint " + adapterRequest.getEndPoint());
			final String camelCircleEndpoint = BusinessConstants.CAMEL_DIRECT + adapterRequest.getEndPoint();
			LOGGER.debug("Endpoint got from Request Adapter :" + camelCircleEndpoint);
			context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			final Endpoint endpoint = context.getEndpoint(camelCircleEndpoint, DirectEndpoint.class);
			final Exchange exchange = endpoint.createExchange();
			final Message in = exchange.getIn();
			final String requestId = adapterRequest.getRequestId();
			final String circleCode = adapterRequest.getCircleCode();
			exchange.setProperty(BusinessConstants.CIRCLE_CODE, circleCode);
			LOGGER.debug("CircleCode got from Request Adapter :" + circleCode);
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, ExternalSystem.EMA);
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			exchange.setProperty(BusinessConstants.MSISDN, fdpRequest.getSubscriberNumber());
			final String inputCommand = httpRequest.toString();
			// String inputCommand = "HGSDP:MSISDN=918129149806,SUDA;\nexit;";
			LOGGER.debug("Request Need to be fire :" + inputCommand);
			final String circleName = fdpRequest.getCircle().getCircleName();
			final String moduleName = BusinessModuleType.EMA_SOUTH.name();
			LOGGER.debug("Module Name:" + moduleName);
			LOGGER.debug("Circle Name :" + circleName);
			final String loggingString = BusinessConstants.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
					+ requestId + FDPConstant.LOGGER_DELIMITER + FDPConstant.EMACMDNM
					+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + adapterRequest.getCommandName()
					+ FDPConstant.LOGGER_DELIMITER + FDPConstant.EMACMD + FDPConstant.LOGGER_KEY_VALUE_DELIMITER
					+ adapterRequest.getLogValue();
			LOGGER.debug("Logging String :" + loggingString);
			final Logger circleLoggerRequest = FDPLoggerFactory.getRequestLogger(circleName, moduleName);
			FDPLogger.info(circleLoggerRequest, getClass(), "callClient()", loggingString);
			in.setBody(inputCommand + "\nexit;");
			String response = null;
			final Producer producer = endpoint.createProducer();
			// producer.start();
			producer.process(exchange);
			final Message out = exchange.getOut();
			response = out.getBody(String.class);
			if (response != null) {
				LOGGER.debug("Response from EMA :" + response);
				FDPLogger.info(circleLoggerRequest, getClass(), "callClient()", BusinessConstants.REQUEST_ID
						+ FDPConstant.LOGGER_KEY_VALUE_DELIMITER + requestId + FDPConstant.LOGGER_DELIMITER
						+ FDPConstant.IFRESMODE + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + FDPConstant.RESULT_SUCCESS);
				responseMap.put(BusinessConstants.RESPONSE_CODE, "200");
				responseMap.put(BusinessConstants.COMMAND_OUTPUT, response);
			}
			LOGGER.debug("Method terminated properly..");
		} catch (final Exception e) {
			LOGGER.error("Exception :", e);
		}
		return responseMap;
	}

	@Override
	public String toString() {
		return " SSH adapter , external system :- " + externalSystem.name() + " circle "
				+ adapterRequest.getCircleCode() + " request string :- " + httpRequest.toString();

	}
}
