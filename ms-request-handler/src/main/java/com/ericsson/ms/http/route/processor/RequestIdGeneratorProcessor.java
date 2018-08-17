package com.ericsson.ms.http.route.processor;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.UUID;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ericsson.ms.common.constants.NBRequestHandlerConstant;
import com.ericsson.ms.common.enums.RequestValuesEnum;
import com.ericsson.ms.common.util.ReqHandlerLoggerUtil;
import com.ericsson.ms.common.util.RequestHandlerLogger;
import com.ericsson.ms.http.route.framework.AbstractNorthBoundProcessor;

/**
 * The RequestIdGeneratorProcessor class
 * 
 * @author Ericsson
 *
 */
@Service
@Scope(value = "prototype")
public class RequestIdGeneratorProcessor extends AbstractNorthBoundProcessor {

	private static final Logger logger = LoggerFactory.getLogger(RequestIdGeneratorProcessor.class);

	@Override
	public void process(Exchange exchange) throws Exception {

		String action = exchange.getIn().getHeader(RequestValuesEnum.ACTION.getValue(), String.class);
		String requestId = generateRequestId(action);
		exchange.getIn().setHeader(NBRequestHandlerConstant.REQUEST_ID, requestId);
		RequestHandlerLogger handlerLogger = new RequestHandlerLogger.RequestHandlerLoggerBuilder(requestId)
				.setMessage("Generating request Id ")
				.setMsisdn(exchange.getIn().getHeader(RequestValuesEnum.MSISDN.getValue(), String.class)).build();
		ReqHandlerLoggerUtil.info(logger, getClass(), "process()", handlerLogger.toString());

	}

	/**
	 * Generate request id.
	 * 
	 * @param action
	 *            the action name
	 * @return the string
	 * @throws UnknownHostException
	 *             the unknown host exception
	 */
	private String generateRequestId(final String action) throws UnknownHostException {
		return new StringBuilder(action).append(NBRequestHandlerConstant.UNDERSCORE)
				.append(Inet4Address.getLocalHost().getHostAddress()).append(NBRequestHandlerConstant.UNDERSCORE)
				.append(String.valueOf(UUID.randomUUID())).toString();
	}

}
