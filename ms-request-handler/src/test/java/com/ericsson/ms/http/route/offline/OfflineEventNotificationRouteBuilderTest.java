package com.ericsson.ms.http.route.offline;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

import com.ericsson.ms.http.response.MSRequestHandlerResponse;
import com.ericsson.ms.http.route.processor.CheckMandatoryParamProcessor;
import com.ericsson.ms.http.route.processor.EventNotificationProcessor;
import com.ericsson.ms.http.route.processor.MSResponseProcessor;
import com.ericsson.ms.http.route.processor.RequestIdGeneratorProcessor;

/**
 * 
 * this class is used to test the OfflineEventNotification route
 * 
 * @author ERICSSON
 */
public class OfflineEventNotificationRouteBuilderTest extends CamelTestSupport {

	/**
	 * The ProducerTemplate
	 */
	@Produce(uri = "direct:start")
	private ProducerTemplate template;

	/**
	 * The MockEndPoint
	 */
	@EndpointInject(uri = "mock:result")
	private MockEndpoint resultEndpoint;

	/**
	 * method is used to test the success case
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSuccessParams() throws Exception {

		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("action", "NOMENURENEWAL");
		requestParams.put("productName", "CIS product");
		requestParams.put("productID", "1234");
		requestParams.put("msisdn", "1213212");
		requestParams.put("expiryDate", "12/01/2020");
		requestParams.put("amount", "100");

		resultEndpoint.setResultWaitTime(1000);
		String expectedBody = "SUCCESS";
		MSRequestHandlerResponse msRequestHandlerResponse = (MSRequestHandlerResponse) template
				.requestBodyAndHeaders(Exchange.HTTP_QUERY, requestParams);
		assertEquals(expectedBody, msRequestHandlerResponse.getStatus());
		assertEquals(expectedBody.toLowerCase(), msRequestHandlerResponse.getResponseDesc());
		assertEquals("200", msRequestHandlerResponse.getResponseCode().toString());
	}

	/**
	 * method is used for test mandatory params i.e. action
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMissingMandatoryActionParams() throws Exception {
		Map<String, Object> requestParams = new HashMap<>();
		// inputMap.put("action", "NOMENURENEWAL");
		requestParams.put("productName", "CIS product");
		requestParams.put("productID", "1234");
		requestParams.put("msisdn", "1213212");
		requestParams.put("expiryDate", "12/01/2020");
		requestParams.put("amount", "100");
		String expectedBody = "FAILURE";
		resultEndpoint.setResultWaitTime(1000);
		MSRequestHandlerResponse msRequestHandlerResponse = (MSRequestHandlerResponse) template
				.requestBodyAndHeaders(Exchange.HTTP_QUERY, requestParams);
		assertEquals(expectedBody, msRequestHandlerResponse.getStatus());
		assertEquals("Invalid Request parameter", msRequestHandlerResponse.getResponseDesc());
		assertEquals("401", msRequestHandlerResponse.getResponseCode().toString());
	}

	/**
	 * method is used for test mandatory params i.e. msisdn
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMissingMandatoryMsisdnParams() throws Exception {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("action", "NOMENURENEWAL");
		requestParams.put("productName", "CIS product");
		requestParams.put("productID", "1234");
//		requestParams.put("msisdn", "1213212");
		requestParams.put("expiryDate", "12/01/2020");
		requestParams.put("amount", "100");
		String expectedBody = "FAILURE";
		resultEndpoint.setResultWaitTime(1000);
		MSRequestHandlerResponse msRequestHandlerResponse = (MSRequestHandlerResponse) template
				.requestBodyAndHeaders(Exchange.HTTP_QUERY, requestParams);
		assertEquals(expectedBody, msRequestHandlerResponse.getStatus());
		assertEquals("Invalid Request parameter", msRequestHandlerResponse.getResponseDesc());
		assertEquals("401", msRequestHandlerResponse.getResponseCode().toString());
	}
	
	/**
	 * method is used for test mandatory params i.e. expiry date
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMissingMandatoryExpiryDateParams() throws Exception {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("action", "NOMENURENEWAL");
		requestParams.put("productName", "CIS product");
		requestParams.put("productID", "1234");
		requestParams.put("msisdn", "1213212");
//		requestParams.put("expiryDate", "12/01/2020");
		requestParams.put("amount", "100");
		String expectedBody = "FAILURE";
		resultEndpoint.setResultWaitTime(1000);
		MSRequestHandlerResponse msRequestHandlerResponse = (MSRequestHandlerResponse) template
				.requestBodyAndHeaders(Exchange.HTTP_QUERY, requestParams);
		assertEquals(expectedBody, msRequestHandlerResponse.getStatus());
		assertEquals("Invalid Request parameter", msRequestHandlerResponse.getResponseDesc());
		assertEquals("401", msRequestHandlerResponse.getResponseCode().toString());
	}
	
	/**
	 * method is used for test mandatory params i.e. ammount
	 * 
	 * @throws Exception
	 */
	@Test
	public void testMissingMandatoryAmmountParams() throws Exception {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("action", "NOMENURENEWAL");
		requestParams.put("productName", "CIS product");
		requestParams.put("productID", "1234");
		requestParams.put("msisdn", "1213212");
		requestParams.put("expiryDate", "12/01/2020");
//		requestParams.put("amount", "100");
		String expectedBody = "FAILURE";
		resultEndpoint.setResultWaitTime(1000);
		MSRequestHandlerResponse msRequestHandlerResponse = (MSRequestHandlerResponse) template
				.requestBodyAndHeaders(Exchange.HTTP_QUERY, requestParams);
		assertEquals(expectedBody, msRequestHandlerResponse.getStatus());
		assertEquals("Invalid Request parameter", msRequestHandlerResponse.getResponseDesc());
		assertEquals("401", msRequestHandlerResponse.getResponseCode().toString());
	}
	
	/**
	 * method is used for test empty params i.e. action
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEmptyMandatoryAmmountParams() throws Exception {
		Map<String, Object> requestParams = new HashMap<>();
		requestParams.put("action", "");
		requestParams.put("productName", "CIS product");
		requestParams.put("productID", "1234");
		requestParams.put("msisdn", "1213212");
		requestParams.put("expiryDate", "12/01/2020");
		requestParams.put("amount", "100");
		String expectedBody = "FAILURE";
		resultEndpoint.setResultWaitTime(1000);
		MSRequestHandlerResponse msRequestHandlerResponse = (MSRequestHandlerResponse) template
				.requestBodyAndHeaders(Exchange.HTTP_QUERY, requestParams);
		assertEquals(expectedBody, msRequestHandlerResponse.getStatus());
		assertEquals("Invalid Request parameter", msRequestHandlerResponse.getResponseDesc());
		assertEquals("401", msRequestHandlerResponse.getResponseCode().toString());
	}
	
	/*
	 * create route builder (non-Javadoc)
	 * 
	 * @see org.apache.camel.test.junit4.CamelTestSupport#createRouteBuilder()
	 */
	@Override
	protected RouteBuilder createRouteBuilder() {

		return new RouteBuilder() {

			@SuppressWarnings("unchecked")
			@Override
			public void configure() throws Exception {
				from("direct:start")
						.setExchangePattern(ExchangePattern.InOut)
						.autoStartup(true)
						.onException(
								org.apache.camel.http.common.HttpOperationFailedException.class,
								java.net.SocketException.class,
								javax.jms.JMSException.class).handled(true)
						.process(new MSResponseProcessor()).end()
						.process(new CheckMandatoryParamProcessor())
						.process(new RequestIdGeneratorProcessor())
						.process(new EventNotificationProcessor())
						.to("mock:result").process(new MSResponseProcessor())
						.end();

			}

		};
	}

}
