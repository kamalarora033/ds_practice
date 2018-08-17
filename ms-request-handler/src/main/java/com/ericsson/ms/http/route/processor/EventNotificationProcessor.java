package com.ericsson.ms.http.route.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.ericsson.ms.common.constants.NBRequestHandlerConstant;
import com.ericsson.ms.common.constants.RoutingConstant;
import com.ericsson.ms.common.enums.RequestValuesEnum;
import com.ericsson.ms.common.util.ReqHandlerLoggerUtil;
import com.ericsson.ms.common.util.RequestHandlerLogger;
import com.ericsson.ms.http.model.MSHttpRequest;
import com.ericsson.ms.http.route.framework.AbstractNorthBoundProcessor;

/**
 * @author GUR51924
 * 
 */
@Service
@Scope(value = "prototype")
public class EventNotificationProcessor extends AbstractNorthBoundProcessor {

	private static final Logger logger = LoggerFactory.getLogger(EventNotificationProcessor.class);

	@Override
	public void process(Exchange objExchange) throws Exception {

		RequestHandlerLogger handlerLogger = new RequestHandlerLogger.RequestHandlerLoggerBuilder(
				objExchange.getIn().getHeader(NBRequestHandlerConstant.REQUEST_ID, String.class))
						.setMessage("Processing Event ")
						.setMsisdn(objExchange.getIn().getHeader(RequestValuesEnum.MSISDN.getValue(), String.class))
						.build();
		ReqHandlerLoggerUtil.debug(logger, getClass(), "process()", handlerLogger.toString());
		objExchange.getIn().setHeader(RoutingConstant.PUSH_TO_QUEUE_HEADER, Boolean.TRUE);
		objExchange.getIn().setBody(updateParameters(objExchange));

	}

	/**
	 * Return the MSHttprequest Object after setting value from Exchange
	 * 
	 * @param exchange
	 * @return the MSHttpRequest Param
	 */
	private MSHttpRequest updateParameters(Exchange exchange) {
		MSHttpRequest msHttpRequest = new MSHttpRequest();
		Message message = exchange.getIn();
		msHttpRequest.setRequestId(message.getHeader(NBRequestHandlerConstant.REQUEST_ID, String.class));
		msHttpRequest.setProductName(message.getHeader(RequestValuesEnum.PRODUCT_NAME.getValue(), String.class));
		msHttpRequest.setProductID(message.getHeader(RequestValuesEnum.PRODUCT_ID.getValue(), String.class));
		msHttpRequest.setAmount(message.getHeader(RequestValuesEnum.AMOUNT.getValue(), String.class));
		msHttpRequest.setExpiryDate(message.getHeader(RequestValuesEnum.EXPIRY_DATE.getValue(), String.class));
		msHttpRequest.setMsisdn(message.getHeader(RequestValuesEnum.MSISDN.getValue(), String.class));
		msHttpRequest.setAction(message.getHeader(RequestValuesEnum.ACTION.getValue(), String.class));
		return msHttpRequest;
	}

}
