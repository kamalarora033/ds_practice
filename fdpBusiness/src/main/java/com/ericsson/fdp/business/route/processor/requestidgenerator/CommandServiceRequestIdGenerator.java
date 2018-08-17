package com.ericsson.fdp.business.route.processor.requestidgenerator;

import org.apache.camel.Exchange;

import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.route.request.idgenerator.AbstractRequestIdGenerator;

/**
 * The Class CommandServiceRequestIdGenerator.
 * 
 * @author Ericsson
 */
public class CommandServiceRequestIdGenerator extends AbstractRequestIdGenerator {

	/** The Constant DEFAULT_COMMAND_SERVICE_INAME. */
	private static final String DEFAULT_COMMAND_SERVICE_INAME = "IVR";

	@Override
	public String getChannelName(final Exchange exchange) {
		final String iName = exchange.getIn().getHeader(FulfillmentParameters.INVOCATOR_NAME.getValue(), String.class);
		return (iName == null) ? DEFAULT_COMMAND_SERVICE_INAME : iName;
	}

}
