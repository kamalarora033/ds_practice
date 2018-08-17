package com.ericsson.fdp.business.route.processor.ivr.impl;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.enums.ivr.FulfillmentRouteEnum;
import com.ericsson.fdp.business.route.processor.ivr.AbstractFulfillmentProcessor;

/**
 * IVRMandatoryParameterProcessor checks the mandatory parameters based on the
 * ivr request and send the response back if any of the mandatory parameter is
 * missing.
 * 
 * @author Ericsson
 */
public class FulfillmentMandatoryParameterProcessor extends AbstractFulfillmentProcessor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(FulfillmentMandatoryParameterProcessor.class);
	
	@Override
	public void process(final Exchange exchange) throws Exception {
		final FulfillmentRouteEnum ivrRoute = getRouteInfo(exchange);
		LOGGER.debug("Checking Mandatory parameters.");
		LOGGER.debug("All Mandatory parameters found.");
		checkMandatoryParametersAndSendResponse(exchange, ivrRoute.getParameterList());
	}

	

}
