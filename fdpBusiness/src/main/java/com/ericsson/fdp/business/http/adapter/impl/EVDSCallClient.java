package com.ericsson.fdp.business.http.adapter.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import com.ericsson.fdp.business.https.evds.HTTPSServerDetailsDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPEVDSConfigDTO;
import com.ericsson.fdp.dao.dto.FDPExternalSystemDTO;
import com.ericsson.fdp.dao.enums.ExternalSystem;
import com.ericsson.fdp.dao.fdpadmin.FDPEVDSConfigDAO;

public class EVDSCallClient extends AbstractAdapterHttpCallClient{
	@Inject
	private FDPEVDSConfigDAO fdpEVDSConfigDAO;
	private static final String evdsType = PropertyUtils
			.getProperty("evds.protocol.type");
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EVDSCallClient.class);
	
	/** The context. */
	private CdiCamelContext context;
	
	@Override
	public Map<String, Object> httpCallClient(final String request, final HttpAdapterRequest httpAdapterRequest,
			final ExternalSystem externalSystemType, final FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<String, Object> responseMap = null;
		Endpoint endpoint = null;
		try {
		     context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			if(evdsType.equalsIgnoreCase((String) FDPConstant.EVDS_TYPE_HTTP)){
			endpoint = context.getEndpoint(BusinessConstants.HTTP_COMPONENT_EVDS_ENDPOINT, DirectEndpoint.class);	
			}
			else if(evdsType.equalsIgnoreCase((String) FDPConstant.EVDS_HTTP_TYPE)){
				endpoint = context.getEndpoint(BusinessConstants.HTTP_COMPONENT_EVDS_ENDPOINT, DirectEndpoint.class);	
				}
			else{
			endpoint = context.getEndpoint(BusinessConstants.NETTY_COMPONENT_EVDS_ENDPOINT, DirectEndpoint.class);
			}
			LOGGER.debug("EVDS Request to be post on endpoint :" + endpoint.getEndpointUri());
			final Exchange exchange = endpoint.createExchange();
			exchange.setPattern(ExchangePattern.InOut);
			final Message in = exchange.getIn();
			in.setHeader(Exchange.CONTENT_LENGTH, request.length());
			exchange.setProperty(BusinessConstants.CIRCLE_CODE, httpAdapterRequest.getCircleCode());
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, externalSystemType.name());
			String requestId = httpAdapterRequest.getRequestId();
			String circleName = httpAdapterRequest.getCircleName();
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			in.setBody(request.toString());
			final Producer producer = endpoint.createProducer();
			producer.process(exchange);
			final Message out = exchange.getOut();
			String outputXML = out.getBody(String.class); 
			String responseCode = (outputXML != null && !outputXML.trim().isEmpty()) ? "200" : null;
			responseMap = new HashMap<String, Object>();
			Logger logger = getLogger(circleName, externalSystemType);
			if (responseCode != null && "200".equals(responseCode)) {
				postProcessOfLogsSuccess(logger, requestId);
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
			AppCacheSubStore appCacheSubStoreKey = AppCacheSubStore.EVDS_DETAILS;
			appBag2.setSubStore(appCacheSubStoreKey);
			appBag2.setKey(outGoingcircleCodeIPaddressPort);
			final FDPExternalSystemDTO externalSystemCacheBean = (FDPExternalSystemDTO) ApplicationConfigUtil
					.getApplicationConfigCache().getValue(appBag2);
			//final String logicalName = externalSystemCacheBean.getLogicalName();
			stringBuilder.append(FDPConstant.CHARGING_NODE_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(outGoingcircleCodeIPaddressPort)
					.append(FDPConstant.LOGGER_DELIMITER + FDPConstant.INTERFACE_TYPE)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(externalSystemType)
					.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHARGING_NODE)
					//.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(logicalName)
					.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.IFRESRSN)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append("Error in exchange is ")
					.append(exchange.getException());
		} else {
			stringBuilder.append("Could not get out going circle code and ip.");
		}
		FDPLogger.error(logger, getClass(), "process()", stringBuilder.toString());
	}

	/**
	 * Gets the endpoint urls for EVDS by circle code.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the endpoint urls for EVDS by circle code
	 */
	
	private List<FDPEVDSConfigDTO> getEndpointURLsForEVDSByCircleCode(final String circleCode) {
		return fdpEVDSConfigDAO.getEVDSEndpointByCircleCode(circleCode);
	}
	
	/**
	 * Provide mapping between DTOs 
	 * @param fdpevdsConfigDTO
	 * @return
	 */
	private HTTPSServerDetailsDTO mappEVDSHTTPSSeverDetails(
			FDPEVDSConfigDTO fdpevdsConfigDTO) {
		HTTPSServerDetailsDTO httpsServerDetailsdto=new HTTPSServerDetailsDTO();
		httpsServerDetailsdto.setContext(fdpevdsConfigDTO.getContextPath());
		httpsServerDetailsdto.setIp(fdpevdsConfigDTO.getIpAddress().getValue());
		//httpsServerDetailsdto.setAcceptlanguage(fdpevdsConfigDTO.getacceptlanguage);
		httpsServerDetailsdto.setLogicalname(fdpevdsConfigDTO.getLogicalName());
		httpsServerDetailsdto.setPort(fdpevdsConfigDTO.getPort());
		httpsServerDetailsdto.setTimeout(fdpevdsConfigDTO.getResponseTimeout());
		httpsServerDetailsdto.setIsenabled(fdpevdsConfigDTO.getIsActive());
		httpsServerDetailsdto.setUseragent(fdpevdsConfigDTO.getUserAgent());
		return httpsServerDetailsdto;
		
	}
}
