package com.ericsson.fdp.business.route.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

import com.ericsson.fdp.business.constants.BusinessConstants;

public class RemoveHeaders implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		Message in = exchange.getIn();
		in.removeHeader(BusinessConstants.BREAD_CRUMB_ID);
		in.removeHeader(BusinessConstants.CIRCLE_CODE);
		in.removeHeader(BusinessConstants.OUTGOING_CIRCLE_CODE_IP_PORT);
		in.removeHeader(BusinessConstants.REQUEST_ID);
		in.removeHeader(BusinessConstants.OUTGOING_IP_ADDRESS);
		in.removeHeader(BusinessConstants.EXTERNAL_SYSTEM_TYPE);
		
		
	}

}
