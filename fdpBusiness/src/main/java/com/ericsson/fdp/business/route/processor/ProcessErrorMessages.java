package com.ericsson.fdp.business.route.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.smpp.SmppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;

/**
 * The Class {@link org.apache.camel.Processor} based ProcessErrorMessages is
 * capable of process the exception messages which are catch by deadletterqueue.
 * 
 * @author Ericsson
 */
// @Named
public class ProcessErrorMessages implements Processor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProcessErrorMessages.class);

	@Override
	public void process(final Exchange exchange) throws Exception {

		String body = exchange.getIn().getBody(String.class);

		LOGGER.info(
				"Error While Processing the message :{} | MSISDN : {} | Request ID : {} ",
				new Object[] {
						body,
						exchange.getIn().getHeader(SmppConstants.SOURCE_ADDR),
						exchange.getIn()
								.getHeader(BusinessConstants.REQUEST_ID) });
		exchange.getOut().setBody("Error While Processing the message " + body);
		exchange.getOut().setHeaders(exchange.getIn().getHeaders());

	}

}
