package com.ericsson.fdp.business.service.impl;

import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.component.smpp.SmppConstants;
import org.jboss.ejb3.annotation.Clustered;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.logging.USSDTrafficLoggerPosition;
import com.ericsson.fdp.route.cdi.context.provider.CdiCamelContextProvider;
import com.ericsson.fdp.route.constant.RoutingConstant;
import com.ericsson.fdp.route.request.service.RequestService;
import com.ericsson.fdp.smpp.util.SMPPUtil;

/**
 * The Class PushToUSSDInQueueServiceImpl.
 * 
 * @author Ericsson
 */

@Stateless
@Clustered
public class PushToUSSDInQueueServiceImpl implements RequestService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PushToUSSDInQueueServiceImpl.class);

	/** The cdi camel context provider. */
	@Inject
	private CdiCamelContextProvider cdiCamelContextProvider;

	@Override
	public void request(final Object body, final Map<String, Object> headers) throws Exception {
		if (headers != null) {
			final Object requestId = headers.get("RID");
			LOGGER.info("{} | Business requested with requestId : {}, headers : {}", new Object[] { this.getClass(),
					requestId, headers });
			final String circleCode = (String) headers.get(RoutingConstant.CIRCLE_ID);
			final String compiledQueueName = SMPPUtil.getUSSDInQueue(circleCode);
			LOGGER.debug("{} | Pushing message to queue : {}, circleCode : {} with requestId : {}",
					new Object[] { this.getClass(), compiledQueueName, circleCode, requestId });
			final Endpoint queue = cdiCamelContextProvider.getContext().getEndpoint(compiledQueueName);
			queue.createProducer().process(createExchange(queue, body, headers));
			FDPLoggerFactory.reportUssdTarfficReportLogger(getClass(), "request", requestId.toString(), (String) headers.get(SmppConstants.SOURCE_ADDR), "Pushing to InQueue:"+compiledQueueName+", body:"+body+", header:"+headers, USSDTrafficLoggerPosition.PUSHING_REQUEST_TO_INBOUND_QUEUE);
		}
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

}
