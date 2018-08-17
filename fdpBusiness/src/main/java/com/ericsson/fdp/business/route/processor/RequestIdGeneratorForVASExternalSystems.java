package com.ericsson.fdp.business.route.processor;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.dao.entity.ExternalSystemType;
import com.ericsson.fdp.route.constant.RoutingConstant;

/**
 * A Processor based {@link org.apache.camel.Processor} which is capable of
 * processing to generate Request Id for MO that comes to FDP.
 * 
 * @author Ericsson
 */
public class RequestIdGeneratorForVASExternalSystems implements Processor {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(RequestIdGeneratorForVASExternalSystems.class);

	@Override
	public void process(final Exchange exchange) throws Exception {

		// Request ID is the combination of HostIP and UUID separated by
		// underscore(_)
		Message in = exchange.getIn();
		String systemType = in.getHeader(BusinessConstants.SYSTEM_TYPE, String.class);
		final String requestId = generateRequestId(systemType);
		FDPLogger.info(LOGGER, getClass(), "process()", String.format("Generated Request ID : %s", requestId));
		exchange.getIn().setHeader(RoutingConstant.REQUEST_ID, requestId);
	}

	private String generateRequestId(String systemType) throws UnknownHostException {
		final String systemTypeValue = ExternalSystemType.getSystemTypeEnum(systemType).getValue();
		return new StringBuilder(systemTypeValue).append(FDPConstant.UNDERSCORE)
				.append(Inet4Address.getLocalHost().getHostAddress()).append(FDPConstant.UNDERSCORE)
				.append(String.valueOf(UUID.randomUUID())).toString();

	}
}
