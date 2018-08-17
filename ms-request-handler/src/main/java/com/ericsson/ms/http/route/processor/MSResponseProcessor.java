package com.ericsson.ms.http.route.processor;

import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ericsson.ms.common.constants.NBRequestHandlerConstant;
import com.ericsson.ms.common.enums.NorthBoundResponseCode;
import com.ericsson.ms.common.enums.RequestValuesEnum;
import com.ericsson.ms.common.util.ReqHandlerLoggerUtil;
import com.ericsson.ms.common.util.RequestHandlerLogger;
import com.ericsson.ms.common.util.RequestHandlerLogger.RequestHandlerLoggerBuilder;
import com.ericsson.ms.http.route.framework.AbstractNorthBoundProcessor;

/**
 * The MS Response Processor
 * 
 * @author Ericsson
 *
 */
@Service
@Scope(value = "prototype")
public class MSResponseProcessor extends AbstractNorthBoundProcessor {

	private static final Logger logger = LoggerFactory.getLogger(EventNotificationProcessor.class);

	private static final String PROCESS = "process()";

	@Override
	public void process(Exchange exchange) throws Exception {
		RequestHandlerLoggerBuilder handlerLoggerBuilder = new RequestHandlerLogger.RequestHandlerLoggerBuilder(
				exchange.getIn().getHeader(NBRequestHandlerConstant.REQUEST_ID, String.class))
						.setMsisdn(exchange.getIn().getHeader(RequestValuesEnum.MSISDN.getValue(), String.class));

		if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) == null) {
			sendResponse(exchange, NorthBoundResponseCode.SUCCESS, null, NBRequestHandlerConstant.SUCCESS);
			String message = handlerLoggerBuilder.setMessage(NBRequestHandlerConstant.SUCCESS_PUSH_QUEUE).build()
					.toString();
			ReqHandlerLoggerUtil.info(logger, getClass(), PROCESS, message);
		} else {
			if (exchange.getProperty(Exchange.EXCEPTION_CAUGHT) instanceof Exception)
				ReqHandlerLoggerUtil.debug(logger, getClass(), PROCESS,
						((Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT)).getMessage());
			sendResponse(exchange, NorthBoundResponseCode.FAILURE, null, NBRequestHandlerConstant.FAILURE);
			String message = handlerLoggerBuilder.setMessage(NBRequestHandlerConstant.FAILURE_PUSH_QUEUE).build()
					.toString();
			ReqHandlerLoggerUtil.info(logger, getClass(), PROCESS, message);
		}
	}

}
