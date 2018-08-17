package com.ericsson.fdp.business.http.adapter.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.cdi.CdiCamelContext;
import org.apache.camel.component.direct.DirectEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.adapter.impl.AbstractAdapterHttpCallClient;
import com.ericsson.fdp.business.bean.HttpAdapterRequest;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * The Class CallClientADC is used to fire get Handset detail call for ADC External Systems and gets response  from those systems.
 */
public class CallClientADC extends AbstractAdapterHttpCallClient {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CallClientADC.class);

	/** The context. */
	private CdiCamelContext context;

	@Override
	public Map<String, Object> httpCallClient(final String httpRequest, final HttpAdapterRequest httpAdapterRequest,
			final ExternalSystem externalSystemType, final FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<String, Object> responseMap = null;
		try {
			context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			Endpoint endpoint = getEndpoint(externalSystemType);
			LOGGER.debug("Http Request to be post on endpoint :" + endpoint.getEndpointUri());
			final Exchange exchange = endpoint.createExchange();
			exchange.setPattern(ExchangePattern.InOut);
			final Message in = exchange.getIn();
			in.setHeader(Exchange.CONTENT_LENGTH, httpRequest.length());
			exchange.setProperty(BusinessConstants.CIRCLE_CODE, httpAdapterRequest.getCircleCode());
			
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, externalSystemType.name());
			String requestId = httpAdapterRequest.getRequestId();
			String circleName = httpAdapterRequest.getCircleName();
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			in.setBody(httpRequest);
			String outputXML;
			final Producer producer = endpoint.createProducer();
			producer.process(exchange);
			final Message out = exchange.getOut();
			outputXML = out.getBody(String.class);
			String responseCode = out.getHeader(BusinessConstants.HTTP_RESPONSE_CODE, String.class);
			responseMap = new HashMap<>();
			Logger logger = getLogger(circleName, externalSystemType);
			if (responseCode != null && "200".equals(responseCode) && outputXML != null) {
				postProcessOfLogsSuccess(logger, requestId, externalSystemType, exchange);
			} else {
				postProcessOfLogsFailure(logger, requestId, exchange);
				postProcessForErrorLogs(exchange, externalSystemType, requestId, logger);
				responseCode = BusinessConstants.HTTP_ADAPTER_ERROR_CODE;
				notRespondingLogger(externalSystemType, endpoint);
			}
			responseMap.put(BusinessConstants.RESPONSE_CODE, responseCode);
			responseMap.put(BusinessConstants.COMMAND_OUTPUT, outputXML);
			FDPLogger.info(logger, getClass(), "httpCallClient()",BusinessConstants.REQUEST_ID + FDPConstant.LOGGER_KEY_VALUE_DELIMITER + 
					requestId + FDPConstant.LOGGER_DELIMITER + " ADC Command Response: " + outputXML);
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			responseMap.put(BusinessConstants.RESPONSE_CODE, "-200");
			responseMap.put(BusinessConstants.COMMAND_OUTPUT, "RESP:-200");
		}
		return responseMap;
	}

	/**
	 * Post process for error logs.
	 * 
	 * @param exchange
	 *            the exchange
	 * @param externalSystemType
	 *            the external system type
	 * @param requestId
	 *            the request id
	 * @param logger
	 *            the logger
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private void postProcessForErrorLogs(final Exchange exchange, final ExternalSystem externalSystemType,
			final String requestId, final Logger logger) throws ExecutionFailedException {
		final FDPAppBag appBag2 = new FDPAppBag();
		final String outGoingcircleCodeIPaddressPort = exchange.getProperty(
				BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, String.class);
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(requestId).append(FDPConstant.LOGGER_DELIMITER);
		if (outGoingcircleCodeIPaddressPort != null) {
			AppCacheSubStore appCacheSubStoreKey = getAppCacheSubStore(externalSystemType);
			appBag2.setSubStore(appCacheSubStoreKey);
			appBag2.setKey(outGoingcircleCodeIPaddressPort);
			final FDPExternalSystemDTO externalSystemCacheBean = (FDPExternalSystemDTO) ApplicationConfigUtil
					.getApplicationConfigCache().getValue(appBag2);
			final String logicalName = externalSystemCacheBean.getLogicalName();
			stringBuilder.append(FDPConstant.CHARGING_NODE_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(outGoingcircleCodeIPaddressPort)
					.append(FDPConstant.LOGGER_DELIMITER + FDPConstant.INTERFACE_TYPE)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(externalSystemType)
					.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHARGING_NODE)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(logicalName)
					.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESRSN)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("Error in exchange is ")
					.append(exchange.getException());
		} else {
			stringBuilder.append("Could not get out going circle code and ip.");
		}
		FDPLogger.error(logger, getClass(), "process()", stringBuilder.toString());
	}

	/**
	 * Gets the endpoint.
	 * 
	 * @param externalSystem
	 *            the external system
	 * @return the endpoint
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private Endpoint getEndpoint(final ExternalSystem externalSystem) throws ExecutionFailedException {
		Endpoint endpoint;
		String endPointName;
		
		endPointName = BusinessConstants.HTTP_COMPONENT_ADC_ENDPOINT;
			
		endpoint = context.getEndpoint(endPointName, DirectEndpoint.class);
		return endpoint;
	}

	/**
	 * Gets the app cache sub store.
	 * 
	 * @param externalSystem
	 *            the external system
	 * @return the app cache sub store
	 */
	private AppCacheSubStore getAppCacheSubStore(final ExternalSystem externalSystem) {
		AppCacheSubStore appCacheSubStore = null;
		
		appCacheSubStore = AppCacheSubStore.ADCCONFEGURATION_MAP;
			
		return appCacheSubStore;
	}
	
	public void postProcessOfLogsSuccess(final Logger circleLoggerRequest, final String requestId, 
			final ExternalSystem externalSystemType, final Exchange exchange) throws ExecutionFailedException {
		final FDPAppBag appBag2 = new FDPAppBag();
		final String outGoingcircleCodeIPaddressPort = exchange.getProperty(
				BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, String.class);
		AppCacheSubStore appCacheSubStoreKey = getAppCacheSubStore(externalSystemType);
		appBag2.setSubStore(appCacheSubStoreKey);
		appBag2.setKey(outGoingcircleCodeIPaddressPort);
		FDPLogger.info(
				circleLoggerRequest,
				getClass(),
				"process()",
				new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(requestId).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_SUCCESS).toString());
	}
}