package com.ericsson.fdp.business.route.processor.requestidgenerator;

import org.apache.camel.Exchange;

import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.route.request.idgenerator.AbstractRequestIdGenerator;

/**
 * The Class BuyProductRequestIdGenerator.
 * 
 * @author Ericsson
 */
public class RequestIdGeneratorForFulfillment extends AbstractRequestIdGenerator {

	/** The Constant DEFAULT_FULFILLMENT_SERVICE_CHANNEL_NAME. */
    private static final String DEFAULT_FULFILLMENT_SERVICE_CHANNEL_NAME = "WEB";
    
	@Override
	public String getChannelName(final Exchange exchange) {
           return DEFAULT_FULFILLMENT_SERVICE_CHANNEL_NAME;
	}
}
