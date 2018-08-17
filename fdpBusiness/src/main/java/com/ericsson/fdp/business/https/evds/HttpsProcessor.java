package com.ericsson.fdp.business.https.evds;

import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpsProcessor implements Processor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpsProcessor.class);
	HTTPSManager httpsmanager;

	public HttpsProcessor() {}

	public HttpsProcessor(List<HTTPSServerDetailsDTO> httpsserverdetailsdtolst) {
		httpsmanager=new HTTPSManager(httpsserverdetailsdtolst);
		}

	@Override
	public void process(Exchange exchange) throws Exception {
		String response=httpsmanager.httpsHit(exchange.getIn().getBody(String.class));
		LOGGER.debug("content getting from https server :"+response);
		exchange.getOut().setBody(response);
	}
}

	