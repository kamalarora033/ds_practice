package com.ericsson.fdp.business.route.processor;

import javax.servlet.ServletRequest;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.ProcessorBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentRouteEnum;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.route.enumeration.FDPRouteHeaders;

public class MobileMoneyWhiteListIpProcessor  extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MobileMoneyWhiteListIpProcessor.class);

	
	@Override
	public void process(Exchange exchange) throws Exception {


		
		final Message in = exchange.getIn();
		final String incomingIpAddress = in.getBody(ServletRequest.class).getRemoteAddr();

		if (incomingIpAddress != null) {
			LOGGER.debug("Validating ip whitelisting for incomingIpAddress.");
			if (!isValidListValueForKey(incomingIpAddress, "MOBILEMONEY_SERVICE_WHITELISTED_IP")) {
				LOGGER.warn("incomingIpAddress value : {} is not whitelisted. So dropping request.", incomingIpAddress);
				exchange.getOut().setHeader(Exchange.HTTP_RESPONSE_CODE, "401");
				exchange.getOut().setBody("");
				exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
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
