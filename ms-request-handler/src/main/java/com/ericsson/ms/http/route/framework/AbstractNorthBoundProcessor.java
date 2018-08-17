package com.ericsson.ms.http.route.framework;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import com.ericsson.ms.common.enums.NorthBoundResponseCode;
import com.ericsson.ms.http.response.MSRequestHandlerResponse;

/**
 * The Abstract North Bound Processor class
 * 
 * @author Ericsson
 *
 */
public abstract class AbstractNorthBoundProcessor implements Processor {

	/**
	 * Set the reponse based on the input parameters ans Stop the Route for
	 * further process
	 * 
	 * @param exchange
	 * @param responseCodes
	 * @param paramName
	 * @param status
	 */
	protected void sendResponseAndStopRoute(Exchange exchange, NorthBoundResponseCode responseCode, String paramName,
			String status) {
		String body = responseCode.getResponseDesc();
		body += (paramName != null && !paramName.isEmpty()) ? " : " + paramName : "";
		MSRequestHandlerResponse handlerResponse = new MSRequestHandlerResponse();
		handlerResponse.setResponseCode(responseCode.getResponseCode());
		handlerResponse.setResponseDesc(body);
		handlerResponse.setStatus(status);
		exchange.getOut().setBody(handlerResponse);
		exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
	}

	/**
	 * Set the response based on the input parameters
	 * 
	 * @param exchange
	 * @param responseCodes
	 * @param paramName
	 * @param status
	 */
	protected void sendResponse(Exchange exchange, NorthBoundResponseCode responseCode, String paramName,
			String status) {
		String body = responseCode.getResponseDesc();
		body += (paramName != null && !paramName.isEmpty()) ? " : " + paramName : "";
		MSRequestHandlerResponse handlerResponse = new MSRequestHandlerResponse();
		handlerResponse.setResponseCode(responseCode.getResponseCode());
		handlerResponse.setResponseDesc(body);
		handlerResponse.setStatus(status);
		exchange.getOut().setBody(handlerResponse);
	}
}
