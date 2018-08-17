package com.ericsson.fdp.business.service.impl;

import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.jboss.ejb3.annotation.Clustered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.logging.USSDTrafficLoggerPosition;
import com.ericsson.fdp.dao.dto.ExchangeMessageResponse;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.route.request.service.RequestService;
import com.ericsson.fdp.smpp.util.SMPPUtil;

/**
 * The Class RequestUSSDAdapterServiceImpl.
 * 
 * @author Ericsson
 */

@Stateless
//@Clustered
public class PushToUSSDOutQueueServiceImpl implements RequestService {

	/** The cdi camel context provider. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/CdiCamelContextProvider")
	private CdiCamelContextProvider cdiCamelContextProvider;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PushToUSSDOutQueueServiceImpl.class);

	@Override
	public void request(final Object body, final Map<String, Object> headers) throws Exception {
		final Object requestId = headers.get("RID");
		final String circleCode = (String) headers.get(RoutingConstant.CIRCLE_ID);
		final String compiledQueueName = getQueueName(circleCode);
		LOGGER.debug("{} | Pushing message to queue : {}, circleCode : {} with requestId : {}",
				new Object[] { this.getClass(), compiledQueueName, circleCode, requestId });
		final Endpoint inQueue = getCdiCamelContextProvider().getContext().getEndpoint(compiledQueueName);
		String msisdn = null;
		String requestIdEx = null;
		if(null != body) {
			ExchangeMessageResponse exchangeMessageResponse = (ExchangeMessageResponse) body;
			msisdn = exchangeMessageResponse.getDestinationAddress();
			requestIdEx = exchangeMessageResponse.getRequestId();
		}
		FDPLoggerFactory.reportUssdTarfficReportLogger(getClass(), "request", requestIdEx, msisdn, "Pushing to OutBound Queue:"+compiledQueueName+", body:"+body+", headers:"+headers, USSDTrafficLoggerPosition.PUSHING_RESPONSE_TO_OUTBOUND_QUEUE);
		inQueue.createProducer().process(createExchange(inQueue, body, headers));
		LOGGER.info("{} | USSDAdapter requested with requestId : {}", this.getClass(), requestId);
	}

	/**
	 * Creates the exchange.
	 * 
	 * @param inQueue
	 *            the in queue
	 * @param body
	 *            the body
	 * @param headers
	 *            the headers
	 * @return the exchange
	 */
	private Exchange createExchange(final Endpoint inQueue, final Object body, final Map<String, Object> headers) {
		final Exchange exchange = inQueue.createExchange(ExchangePattern.InOnly);
		final Message in = exchange.getIn();
		in.setBody(body);
		in.setHeaders(headers);
		return exchange;
	}

	/**
	 * Gets the cdi camel context provider.
	 * 
	 * @return the cdi camel context provider
	 */
	private CdiCamelContextProvider getCdiCamelContextProvider() {
		return cdiCamelContextProvider;
	}

	/**
	 * Gets the queue name.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @return the queue name
	 */
	private String getQueueName(final String circleCode) {
		return SMPPUtil.getUSSDOutQueueEndpoint(circleCode);
	}

}
