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
import com.ericsson.fdp.business.bean.RSHttpAdapterRequest;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.FDPLoggerConstants;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.enums.ExternalSystem;

/**
 * The Class HttpCallClientAirCGWRS is used to fire ACIP/UCIP call for AIR / CGW
 * / RS External Systems and gets response XML from those systems.
 */
public class HttpCallClientAirCGWRS extends AbstractAdapterHttpCallClient {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpCallClientAirCGWRS.class);

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
			if (httpAdapterRequest instanceof RSHttpAdapterRequest) {
				RSHttpAdapterRequest rsHttpAdapterRequest = (RSHttpAdapterRequest) httpAdapterRequest;
				String commandName = rsHttpAdapterRequest.getCommandName();
				LOGGER.debug("Command got for RS :{}", commandName);
				exchange.setProperty(BusinessConstants.RS_COMMAND_NAME, commandName);
			}
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, externalSystemType.name());
			String requestId = httpAdapterRequest.getRequestId();
			String circleName = httpAdapterRequest.getCircleName();
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			in.setBody(httpRequest.toString());
			String outputXML = "";
			final Producer producer = endpoint.createProducer();
			producer.process(exchange);
			final Message out = exchange.getOut();
			outputXML = out.getBody(String.class);
			String responseCode = out.getHeader(BusinessConstants.HTTP_RESPONSE_CODE, String.class);
			responseMap = new HashMap<String, Object>();
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
			return responseMap;
		} catch (final ExecutionFailedException e) {
			LOGGER.error(e.getMessage(), e);
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		return null;
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
		Endpoint endpoint = null;
		String endPointName = null;
		switch (externalSystem) {
		case AIR:
			endPointName = BusinessConstants.HTTP_COMPONENT_AIR_ENDPOINT;
			break;
		case CGW:
			endPointName = BusinessConstants.HTTP_COMPONENT_CGW_ENDPOINT;
			break;
		case RS:
			endPointName = BusinessConstants.HTTP_COMPONENT_RS_ENDPOINT;
			break;
		case CMS:
			endPointName=BusinessConstants.HTTP_COMPONENT_CMS_ENDPOINT;
			break;
		default:
			throw new ExecutionFailedException("Endpoint not found cause of Illegal External System Type.");
		}
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
		switch (externalSystem) {
		case AIR:
			appCacheSubStore = AppCacheSubStore.AIRCONFEGURATION_MAP;
			break;
		case CGW:
			appCacheSubStore = AppCacheSubStore.CGWCONFEGURATION_MAP;
			break;
		case RS:
			appCacheSubStore = AppCacheSubStore.RSCONFEGURATION_MAP;
			break;
		case CMS:
			appCacheSubStore = AppCacheSubStore.CMSCONFEGURATION_MAP;
			break;
		default:
			break;
		}
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
		final FDPExternalSystemDTO externalSystemCacheBean = (FDPExternalSystemDTO) ApplicationConfigUtil
				.getApplicationConfigCache().getValue(appBag2);
		final String logicalName = externalSystemCacheBean.getLogicalName();
		FDPLogger.info(
				circleLoggerRequest,
				getClass(),
				"process()",
				new StringBuilder(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(requestId).append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESMODE)
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(FDPConstant.RESULT_SUCCESS).toString());
						//.append(FDPConstant.LOGGER_DELIMITER).toString());
	}
}