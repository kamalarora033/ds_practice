//package com.ericsson.ms.http.route.offline;
//
// 
//
//import javax.xml.bind.JAXBException;
//
//import org.apache.camel.EndpointInject;
//import org.apache.camel.Exchange;
//import org.apache.camel.Produce;
//import org.apache.camel.ProducerTemplate;
//import org.apache.camel.ShutdownRoute;
//import org.apache.camel.builder.RouteBuilder;
//import org.apache.camel.component.mock.MockEndpoint;
//import org.apache.camel.test.junit4.CamelTestSupport;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.ericsson.ms.http.route.processor.EventNotificationProcessor;
//
//public class EventNotificationRouteTest extends CamelTestSupport {
//
//	@Produce(uri = "direct:start")
//	protected ProducerTemplate template;
//
//	@EndpointInject(uri = "mock:result")
//	protected MockEndpoint resultEndpoint;
//
//	@Before
//	public void setup() throws Exception {
//		super.setUp();
//	}
//
//	/**
//	 * @throws Exception
//	 *             this method is used for test the invalid action parameter
//	 *             case testing
//	 */
////	@Test
////	public void invalidActionParameter() throws Exception {
////		String requestBody = "<HttpRequestParams><action>23</action>"
////				+ "<productName>CIS product</productName>"
////				+ "<productID>1234</productID>" + "<msisdn>1213212</msisdn>"
////				+ "<expiryDate>12/01/2020</expiryDate>"
////				+"<ammount>100.0</ammount>"
////				+ "</HttpRequestParams>";
////		// resultEndpoint.expectedMessageCount(1);
////		resultEndpoint.setResultWaitTime(1000);
////		resultEndpoint.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE,
////				"200");
////		template.sendBodyAndHeader(requestBody, Exchange.HTTP_RESPONSE_CODE,
////				"200");
////		resultEndpoint.assertIsSatisfied();
////
////	}
//
//	/**
//	 * @throws Exception
//	 *             this method is used for test success cases
//	 */
//	@Test
//	public void successParameter() throws Exception {
//		String requestMessage = "<HttpRequestParams><action>MENUONEOFF</action>"
//				+ "<productName>CIS product</productName>"
//				+ "<productID>1234</productID>"
//				+ "<msisdn>1213212</msisdn>"
//				+ "<expiryDate>12/01/2020</expiryDate>"
//				+"<ammount>100.0</ammount>"
//				+ "</HttpRequestParams>";
//		// resultEndpoint.expectedMessageCount(1);
//		resultEndpoint.setResultWaitTime(1000);
//		resultEndpoint.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE,
//				"200");
//		template.sendBodyAndHeader(requestMessage, Exchange.HTTP_RESPONSE_CODE,
//				"200");
//		resultEndpoint.assertIsSatisfied();
//
//	}
//
//	/**
//	 * @throws Exception
//	 *             this method is used for test request body is null or not
//	 */
////	@Test
////	public void isHttpRequestBodyNullOrBlank() throws Exception {
////		String requestBody = ""; // request body is null or blank
////		// resultEndpoint.expectedMessageCount(1);
////		resultEndpoint.setResultWaitTime(1000);
////		resultEndpoint.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE,
////				"200");
////		template.sendBodyAndHeader(requestBody, Exchange.HTTP_RESPONSE_CODE,
////				"200");
////		resultEndpoint.assertIsSatisfied();
////
////	}
//
//	/**
//	 * @throws Exception
//	 *             this method is used for test msisdn is null or blank
//	 */
////	@Test
////	public void isMsisdnBlankOrNull() throws Exception {
////		String requestBody = "<HttpRequestParams><action>MENURENEWAL</action>"
////				+ "<productName>CIS product</productName>"
////				+ "<productID>1234</productID>" + "<msisdn></msisdn>"
////				+ "<expiryDate>12/01/2020</expiryDate>"
////				+ "</HttpRequestParams>";
////		// resultEndpoint.expectedMessageCount(1);
////		resultEndpoint.setResultWaitTime(1000);
////		resultEndpoint.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE,
////				"200");
////		template.sendBodyAndHeader(requestBody, Exchange.HTTP_RESPONSE_CODE,
////				"200");
////		resultEndpoint.assertIsSatisfied();
////
////	}
//
//	/**
//	 * @throws JAXBException 
//	 * @throws Exception
//	 *             this method is used for check all optional params in request
//	 *             body
//	 */
////	@Test
////	public void checkAllOptionalParams() throws Exception {
////		String requestBody = "<HttpRequestParams><action>NOMENURENEWAL</action>"
////				+ "<productName></productName>" + "<productID></productID>"
////				+ "<msisdn>123456789</msisdn>" + "<expiryDate></expiryDate>"
////				+ "</HttpRequestParams>";
////		// resultEndpoint.expectedMessageCount(1);
////		resultEndpoint.setResultWaitTime(1000);
////		resultEndpoint.expectedHeaderReceived(Exchange.HTTP_RESPONSE_CODE,
////				"200");
////		template.sendBodyAndHeader(requestBody, Exchange.HTTP_RESPONSE_CODE,
////				"200");
////		resultEndpoint.assertIsSatisfied();
////
////	}
//	
//	
// 
//	@Override
//	protected RouteBuilder createRouteBuilder() {
//		
//		
//		return new RouteBuilder() {
//			public void configure() {
//				from("direct:start")
//						.autoStartup(true)
//						.shutdownRoute(ShutdownRoute.Default)
//						.onCompletion()
//						.onFailureOnly()						
//						.end()
//						.onCompletion()
//						.onCompleteOnly()						
//						.end()
//						.process(new EventNotificationProcessor())//.unmarshal(xmlDataFormat)
////						.choice()
////						.when(header(Exchange.HTTP_RESPONSE_CODE).isEqualTo(
////								200)).to(getActivemqQueueName()).end();
//				// .to(getActivemqQueueName());
//				 .to("mock:result");
//			}
//		};
//	}
//
//	private String getActivemqQueueName() {
//		return "activemq:ms.offline.renewal.queue";
//	}
//}
