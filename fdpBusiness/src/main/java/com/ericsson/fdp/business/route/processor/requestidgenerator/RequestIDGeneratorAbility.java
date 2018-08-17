package com.ericsson.fdp.business.route.processor.requestidgenerator;

import org.apache.camel.Exchange;

import com.ericsson.fdp.route.request.idgenerator.AbstractRequestIdGenerator;


public class RequestIDGeneratorAbility extends AbstractRequestIdGenerator{

    private static final String DEFAULT_FULFILLMENT_SERVICE_CHANNEL_NAME = "ABILITY";
    
	@Override
	public String getChannelName(Exchange exchange) {
		// TODO Auto-generated method stub
		return DEFAULT_FULFILLMENT_SERVICE_CHANNEL_NAME;
	}

}
