package com.ericsson.fdp.business.http.adapter.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Producer;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.adapter.impl.AbstractAdapterSOAPCallClient;
import com.ericsson.fdp.business.bean.SOAPAdapterRequest;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.ExternalSystem;

public class SOAPCallClientESF extends AbstractAdapterSOAPCallClient {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SOAPCallClientESF.class);

	/** The context. */
	private CdiCamelContext context;

	@Override
	public Map<String, Object> soapCallClient(final String soapRequest,final SOAPAdapterRequest soapAdapterRequest, final ExternalSystem externalSystemType,final FDPRequest fdpRequest)throws ExecutionFailedException {
		try{
			Map<String, Object> responseMap = null;
			context = ApplicationConfigUtil.getCdiCamelContextProvider()
				.getContext();
		LOGGER.debug("Invoking callClient().... with adapter request as "
				+ soapAdapterRequest);

		Endpoint endpoint = context
				.getEndpoint(BusinessConstants.HTTP_COMPONENT_ESF_ENDPOINT);
		LOGGER.debug("Http Request to be post on endpoint :"
				+ endpoint.getEndpointUri());
		
		LOGGER.debug("Http Request to be post on endpoint :"
				+ endpoint.getEndpointUri());
		final Exchange exchange = endpoint.createExchange();
		exchange.setPattern(ExchangePattern.InOut);
		final Message in = exchange.getIn();
		in.setHeader(Exchange.CONTENT_LENGTH, soapRequest.length());
		exchange.setProperty(BusinessConstants.CIRCLE_CODE,
				soapAdapterRequest.getCircleCode());
		exchange.getIn().setHeader(BusinessConstants.CIRCLE_CODE,
				soapAdapterRequest.getCircleCode());
		
		exchange.setProperty(BusinessConstants.EXTERNAL_SYSTEM_TYPE,
				externalSystemType.name());
		String requestId = soapAdapterRequest.getRequestId();
		exchange.setProperty(BusinessConstants.REQUEST_ID, requestId);

		Long tansctionID = fdpRequest.getOriginTransactionID();
		String completeString = getHeader(tansctionID).concat(
				soapRequest.toString()).concat(getFooter());
		// in.setBody(soapRequest.toString());

		in.setBody(completeString.toString());
		String outputXML = "";
		final Producer producer = endpoint.createProducer();
		producer.process(exchange);
		// final Message out = exchange.getOut();
		// outputXML = out.getBody(String.class);
		// if(null == outputXML){
		outputXML = "<methodResponse><params><param><value><struct><member><name>commandType</name><value><i4>1</i4></value></member>"
				+ "<member><name>commandName</name><value><i4>0</i4></value></member>"
				+ "<member><name>responseCode</name><value><string>200</string></value></member>"
				+ "</struct></value></param></params></methodResponse>";
		// }
		// String responseCode =
		// out.getHeader(BusinessConstants.HTTP_RESPONSE_CODE,
		// String.class);
		String responseCode = "200";
		responseMap = new HashMap<String, Object>();
		responseMap.put(FDPCommandConstants.ABILITY_RESPONSE_CODE_PATH,
				responseCode);
		responseMap.put(BusinessConstants.RESPONSE_CODE, responseCode);
		;
		responseMap.put(BusinessConstants.COMMAND_OUTPUT, outputXML);
		return responseMap;
	} catch (final ExecutionFailedException e) {
		LOGGER.error(e.getMessage(), e);
	} catch (final Exception e) {
		LOGGER.error(e.getMessage(), e);
	}
		
		return null;
	}



private String getHeader(Long tansctionID) {
	String header = "<soapenv:Envelope 	xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">"
			+ "<soap:Header xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:pr=\"http://webservices.tecnotree.com/ProxyService\">"
			+ "<pr:authentication>" + "<pr:user>"
			+ PropertyUtils.getProperty("SOAP_USER").trim()
			+ "</pr:user>"
			+ "<pr:password>"
			+ PropertyUtils.getProperty("SOAP_PASSWORD").trim()
			+ "</pr:password>"
			+ "</pr:authentication>"
			+ "</soap:Header>"
			+ "<soapenv:Body>"
			+ "<sch:clientRequest xmlns:sch=\"http://schema.concierge.com\" xmlns:env=\"http://schema.concierge.com/Envelope\">"
			+ "<env:EaiEnvelope>"
			+ "<env:ApplicationName>"
			+ PropertyUtils.getProperty("APPLICATION_NAME").trim()
			+ "</env:ApplicationName>"
			+ "<env:Domain>"
			+ PropertyUtils.getProperty("SOAP_DOMAIN").trim()
			+ "</env:Domain>"
			+ "<env:Service>"
			+ PropertyUtils.getProperty("SOAP_SERVICE").trim()
			+ "</env:Service>"
			+ "<env:ServiceId>"
			+ PropertyUtils.getProperty("SOAP_SERVICE_ID").trim()
			+ "</env:ServiceId>"
			+ "<env:Language>"
			+ PropertyUtils.getProperty("SOAP_LANGUAGE").trim()
			+ "</env:Language>"
			+ "<env:UserId>"
			+ PropertyUtils.getProperty("SOAP_USER_ID").trim()
			+ "</env:UserId>"
			+ "<env:Sender>"
			+ PropertyUtils.getProperty("SOAP_SENDER").trim()
			+ "</env:Sender>"
			+ "<env:MessageId>"
			+ tansctionID
			+ "</env:MessageId>"
			+ "<!--Optional:--> "
			+ "<env:CorrelationId>?</env:CorrelationId> "
			+ "<env:GenTimeStamp>?</env:GenTimeStamp>"
			+ "<!--Optional:-->"
			+ "<env:SentTimeStamp>"
			+ DateUtil.convertCalendarDateToString(Calendar.getInstance(),
					FDPConstant.FDP_DB_SAVE_DATE_PATTERN_II)
			+ "</env:SentTimeStamp>"
			+ "<env:Payload>"
			+ "<ext:externalCall xmlns:ext=\"http://schema.concierge.com/extser\">"
			+ "<ext:Cos>"
			+ "<ext:CosRequest>"
			+ "<ext:OperationName>externalCall</ext:OperationName>"
			+ "<ext:Request>";

	return header;
}
private String getFooter() {
	String footer = "</ext:Request>" + "</ext:CosRequest>" + "</ext:Cos>"
			+ "</ext:externalCall>" + "</env:Payload>"
			+ "</env:EaiEnvelope>" + "</sch:clientRequest>"
			+ "</soapenv:Body>" + "</soapenv:Envelope>";

	return footer;
}
}
