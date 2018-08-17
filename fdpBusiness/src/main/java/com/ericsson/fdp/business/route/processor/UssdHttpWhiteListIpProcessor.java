package com.ericsson.fdp.business.route.processor;

import javax.servlet.ServletRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.UssdHttpRouteEnum;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;
public class UssdHttpWhiteListIpProcessor extends AbstractFulfillmentProcessor {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentWhiteListIpProcessor.class);

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Message in = exchange.getIn();
		final String incomingIpAddress = in.getBody(ServletRequest.class).getRemoteAddr();

		if (incomingIpAddress != null) {
			final UssdHttpRouteEnum routeInfo = getHttpRouteInfo(exchange);
			LOGGER.debug("Validating ip whitelisting for incomingIpAddress.");
			if (!isValidListValueForKey(incomingIpAddress, routeInfo.getWhiteListIpKey())) {
				LOGGER.warn("incomingIpAddress value : {} is not whitelisted. So dropping request.", incomingIpAddress);
				exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
				throw new ExecutionFailedException("incomingIpAddress value : {} is not whitelisted.");
			} else {
				// INCOMING_IP used for logging
				in.setHeader(FDPRouteHeaders.INCOMING_IP.getValue(), incomingIpAddress);
				LOGGER.debug("incomingIpAddress value : {} is a whitelisted IP.", incomingIpAddress);
			}
		} else {
			throw new ExecutionFailedException("Incoming IP Address not found.");
		}
	}
	
	


}
