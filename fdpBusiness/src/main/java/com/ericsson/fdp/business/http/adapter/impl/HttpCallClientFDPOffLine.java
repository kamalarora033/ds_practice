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

public class HttpCallClientFDPOffLine extends AbstractAdapterHttpCallClient{

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpCallClientCMS.class);

	/** The context. */
	private CdiCamelContext context;

	@Override
	public Map<String, Object> httpCallClient(final String httpRequest, final HttpAdapterRequest httpAdapterRequest,
			final ExternalSystem externalSystemType, final FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			final String codendpointString = getCODOFFLIneEndpoint(httpAdapterRequest.getCircleCode());
			final Endpoint codEndpoint = context.getEndpoint(codendpointString, DirectEndpoint.class);
			LOGGER.debug("Http Request to be post on endpoint :" + codEndpoint.getEndpointUri());
			final Exchange exchange = codEndpoint.createExchange();
			exchange.setPattern(ExchangePattern.InOut);
			final Message in = exchange.getIn();
			in.setHeader(Exchange.CONTENT_LENGTH, httpRequest.length());
			exchange.setProperty(BusinessConstants.CIRCLE_CODE, httpAdapterRequest.getCircleCode());
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, externalSystemType.name());
			String requestId = httpAdapterRequest.getRequestId();
			String circleName = httpAdapterRequest.getCircleName();
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			in.setBody(httpRequest.toString());
			String outputXML = "";
			final Producer producer = codEndpoint.createProducer();
			producer.process(exchange);
			final Message out = exchange.getOut();
			outputXML = out.getBody(String.class);
			String responseCode = out.getHeader(BusinessConstants.HTTP_RESPONSE_CODE, String.class);
			Logger logger = getLogger(circleName, externalSystemType);
			if (responseCode != null && "200".equals(responseCode) && outputXML != null) {
				postProcessOfLogsSuccess(logger, requestId);
			} else {
				postProcessOfLogsFailure(logger, requestId, exchange);
				postProcessForErrorLogs(exchange, externalSystemType, requestId, logger);
				responseCode = BusinessConstants.HTTP_ADAPTER_ERROR_CODE;
				notRespondingLogger(externalSystemType, codEndpoint);
			}
			responseMap.put(BusinessConstants.RESPONSE_CODE, responseCode);
			responseMap.put(BusinessConstants.COMMAND_OUTPUT, outputXML);
		} catch (final ExecutionFailedException e) {
			LOGGER.error(e.getMessage(), e);
			responseMap.put(BusinessConstants.RESPONSE_CODE, BusinessConstants.HTTP_ADAPTER_ERROR_CODE);
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			responseMap.put(BusinessConstants.RESPONSE_CODE, BusinessConstants.HTTP_ADAPTER_ERROR_CODE);
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
	 * @param CircleCode
	 *            
	 * @return the endpoint String
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private String getCODOFFLIneEndpoint(String circleCode){
		return BusinessConstants.HTTP_COMPONENT_FDP_OFFLINE_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode;
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
}