package com.ericsson.fdp.business.http.adapter.impl;

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

import com.ericsson.fdp.business.adapter.impl.AbstractAdapterHttpCallClient;
import com.ericsson.fdp.business.bean.HttpAdapterRequest;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.entity.ExternalSystemDetail;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.ExternalSystem;


/**
 * The Class HttpCallClientMCLoan used to request MCarbon External System for Loan and
 * gets reponse from it.
 * @author evivbeh
 */
public class HttpCallClientMCLoan extends AbstractAdapterHttpCallClient {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpCallClientMCLoan.class);

	/** The context. */
	private CdiCamelContext context;

	@Override
	public Map<String, Object> httpCallClient(final String httpRequest, final HttpAdapterRequest httpAdapterRequest,
			final ExternalSystem externalSystemType, final FDPRequest fdpRequest) throws ExecutionFailedException {
		Map<String, Object> responseMap = new HashMap<String, Object>();
		try {
			context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			LOGGER.debug("Invoking callClient().... with adapter request as " + httpAdapterRequest);
			final String camelCircleEndpoint = getMcLoanEndpoint(httpAdapterRequest.getCircleCode());
			LOGGER.debug("Endpoint got from Request Adapter :" + camelCircleEndpoint);
			context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			final Endpoint vasEndpoint = context.getEndpoint(camelCircleEndpoint, DirectEndpoint.class);
			final Exchange exchange = vasEndpoint.createExchange();
			final Message in = exchange.getIn();
			exchange.setProperty(BusinessConstants.CIRCLE_CODE, httpAdapterRequest.getCircleCode());
			exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE, externalSystemType.name());
			String requestId = httpAdapterRequest.getRequestId();
			exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);
			String msisdn = String.valueOf(fdpRequest.getSubscriberNumber());
			LOGGER.debug("Subscriber from fdpRequest : {}", msisdn);
			exchange.setProperty(BusinessConstants.MSISDN, msisdn);
			LOGGER.debug("Command need to fire :" + httpRequest.toString());
			LOGGER.debug("Compiled URI :" + httpRequest.toString());
			in.setHeader(Exchange.HTTP_QUERY, httpRequest.toString());
			final Producer producer = vasEndpoint.createProducer();
			producer.process(exchange);
			final Message out = exchange.getOut();
			final String outputXML = out.getBody(String.class);
			String responseCode = out.getHeader(BusinessConstants.HTTP_RESPONSE_CODE, String.class);
			String circleName = httpAdapterRequest.getCircleName();
			final Logger logger = getLogger(circleName, externalSystemType);
			if (responseCode != null && "200".equals(responseCode) && outputXML != null) {
				postProcessOfLogsSuccess(logger, requestId);
			} else {
				postProcessOfLogsFailure(logger, requestId, exchange);
				postProcessForErrorLogs(exchange, externalSystemType, requestId, logger);
				responseCode = BusinessConstants.HTTP_ADAPTER_ERROR_CODE;
				notRespondingLogger(externalSystemType, vasEndpoint);
			}
			responseMap.put(BusinessConstants.RESPONSE_CODE, responseCode);
			responseMap.put(BusinessConstants.COMMAND_OUTPUT, outputXML);
		} catch (final ExecutionFailedException e) {
			LOGGER.error(e.getMessage(), e);
			responseMap.put(BusinessConstants.RESPONSE_CODE, BusinessConstants.HTTP_ADAPTER_ERROR_CODE);
		} catch (final Exception e) {
			LOGGER.error(e.getMessage(), e);
			responseMap.put(BusinessConstants.RESPONSE_CODE, BusinessConstants.HTTP_ADAPTER_ERROR_CODE);
			//e.printStackTrace();
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
	 */
	private void postProcessForErrorLogs(final Exchange exchange, final ExternalSystem externalSystemType,
			final String requestId, final Logger logger) {
		final String outGoingcircleCodeIPaddressPort = exchange.getProperty(
				BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT, String.class);
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(BusinessConstants.REQUEST_ID).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
				.append(requestId).append(FDPConstant.LOGGER_DELIMITER);
		if (outGoingcircleCodeIPaddressPort != null) {
			final String logicalName = exchange.getProperty(BusinessConstants.LOGICAL_NAME, String.class);
			stringBuilder.append(FDPConstant.CHARGING_NODE_IP).append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(outGoingcircleCodeIPaddressPort)
					.append(FDPConstant.LOGGER_DELIMITER + FDPConstant.INTERFACE_TYPE)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(externalSystemType)
					.append(FDPConstant.LOGGER_DELIMITER).append(FDPConstant.CHARGING_NODE)
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(logicalName).append("Error in exchange is ")
					.append(exchange.getException());
		} else {
			stringBuilder.append("Could not get out goind circle code and ip.");
		}
		FDPLogger.error(logger, getClass(), "process()", stringBuilder.toString());
	}
	
	private String getMcLoanEndpoint(String circleCode){
		return BusinessConstants.HTTP_COMPONENT_MCLOAN_ENDPOINT + BusinessConstants.UNDERSCORE + circleCode;
	}
}
