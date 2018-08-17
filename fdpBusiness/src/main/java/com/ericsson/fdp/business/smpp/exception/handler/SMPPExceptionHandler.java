package com.ericsson.fdp.business.smpp.exception.handler;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

/**
 * The Class SMPPExceptionHandler is custom exception handler class.
 * 
 * @author Ericsson
 */
public class SMPPExceptionHandler implements Processor{

	@Override
	public void process(Exchange exchange) throws Exception {
		
		String body = exchange.getIn().getBody(String.class);
		Exception exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
		StringBuffer sb = new StringBuffer();
		sb.append("ERROR: ");
		sb.append(exception.getMessage());
		sb.append("\nBODY: ");
		sb.append(body);
		exchange.getIn().setBody(sb.toString());
	}

}
