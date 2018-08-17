package com.ericsson.fdp.business.route.processor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.constants.USSDXmlZTEConstants;
import com.ericsson.fdp.business.constants.XMLResponseConstants;
import com.ericsson.fdp.business.enums.DisplayArea;
import com.ericsson.fdp.business.enums.TLVOptions;
import com.ericsson.fdp.business.menu.FDPDynamicMenu;
import com.ericsson.fdp.business.request.requestString.impl.FDPUSSDRequestStringImpl;
import com.ericsson.fdp.business.service.DynamicMenuItegrator;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.vo.CircleConfigParamDTO;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.display.ResponseMessage;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.smpp.util.SMPPUtil;

import freemarker.template.Configuration;
import freemarker.template.Template;


/**
 * A Processor based {@link org.apache.camel.Processor} which is capable of
 * processing the messages coming from multiple queues to call the Dynamic menu
 * for incoming request.
 * 
 * @author Ericsson
 */

public class UssdHttpRequestProcessorZTE implements Processor {

	/** The dynamic Menu Itegrator. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/DynamicMenuItegratorImpl")
	private DynamicMenuItegrator dynamicMenuItegrator;

	/** The fdp dynamic menu. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/DynamicMenuImpl")
	private FDPDynamicMenu fdpDynamicMenu;

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;
	
	public static int PRETTY_PRINT_INDENT_FACTOR = 4;

	/**
	 * The fdp request cache constant.
	 */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/RequestCacheForUSSD")
	private FDPCache<FDPRequestBag, FDPCacheable> fdpRequestCacheForUSSD;

	/** The fdp request cache for sms. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/RequestCacheForSMS")
	private FDPCache<FDPRequestBag, FDPCacheable> fdpRequestCacheForSMS;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(UssdHttpRequestProcessorZTE.class);

	/** The log method name. */
	/* (non-Javadoc)
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void process(final Exchange exchange) throws Exception {

		String transactionId = null;
		String transactionTime = null;
		String msisdn = null;
		String ussdServiceCode = null;
		String ussdRequestString = null;
		String xmlResponse = null;
		String calledNumber = null;
		try {
			String outputXML=(String)exchange.getIn().getBody();
			String jsonString = processXMLRequest(outputXML);
			LOGGER.debug("XML RPC JSON String ::"+ jsonString);
			UssdZteXMLRequestBean ussdXmlRPCRequestBean = new UssdZteXMLRequestBean();
			
			
			try {


				JsonParser jp = null;
				JsonNode paramNode = null;

				ObjectMapper mapper = new ObjectMapper();
				
				JsonFactory factory = mapper.getJsonFactory();
				jp = factory.createJsonParser(jsonString);

				LOGGER.debug("JSON is :: "+ jp.toString());
				paramNode = mapper.readTree(jp);

				if (paramNode.findValue(USSDXmlZTEConstants.MEMBER_TAG)!=null && paramNode.findValue(USSDXmlZTEConstants.MEMBER_TAG).isArray()) {

					for (JsonNode objNode : paramNode.findValue("member")) {

						Iterator<JsonNode> it = objNode.path(USSDXmlZTEConstants.VALUE_TAG).getElements();

						while (it.hasNext()) {

							JsonNode memberValueNode = it.next();

							if (!memberValueNode.isObject() && !memberValueNode.isArray()) {

								if (objNode.findValue(USSDXmlZTEConstants.NAME_TAG).getTextValue().equalsIgnoreCase(USSDXmlZTEConstants.TRANSACTION_ID)) {

									if(memberValueNode != null && memberValueNode.asText().toString().length() > 0) {
										transactionId = memberValueNode.asText().toString().trim();
										ussdXmlRPCRequestBean.setTransactionId(transactionId);
									}
								}

								else if (objNode.findValue(USSDXmlZTEConstants.NAME_TAG).getTextValue().equalsIgnoreCase(USSDXmlZTEConstants.TRANSACTION_TIME)) {
									transactionTime = memberValueNode.asText().toString().trim();
									ussdXmlRPCRequestBean.setTransactionTime(transactionTime);
								}

								else if(objNode.findValue(USSDXmlZTEConstants.NAME_TAG).getTextValue().equalsIgnoreCase(USSDXmlZTEConstants.MSISDN)){
									msisdn = memberValueNode.asText().toString().trim();
									ussdXmlRPCRequestBean.setMsisdn(msisdn);
								}

								else if(objNode.findValue(USSDXmlZTEConstants.NAME_TAG).getTextValue().equalsIgnoreCase(USSDXmlZTEConstants.USSD_SERVICE_CODE)){
									ussdServiceCode = memberValueNode.asText().toString().trim();
									ussdXmlRPCRequestBean.setUssdServiceCode(ussdServiceCode);
								}
								else if(objNode.findValue(USSDXmlZTEConstants.NAME_TAG).getTextValue().equalsIgnoreCase(USSDXmlZTEConstants.USSD_REQUEST_STRING)){
									ussdRequestString = memberValueNode.asText().toString().trim();
									ussdXmlRPCRequestBean.setUssdRequestString(ussdRequestString);
								}
								/**
								 * ZTE USSD send input request string calledNumber parameter.
								 */
								else if(objNode.findValue(USSDXmlZTEConstants.NAME_TAG).getTextValue().equalsIgnoreCase(USSDXmlZTEConstants.CALLED_NUMBER)){
									calledNumber = memberValueNode.asText().toString().trim();
									//ussdXmlRPCRequestBean.setUssdRequestString(calledNumber);
								}
								
								else if(objNode.findValue(USSDXmlZTEConstants.NAME_TAG).getTextValue().equalsIgnoreCase(USSDXmlZTEConstants.XML_RESPONSE)){
									xmlResponse = memberValueNode.asText().toString().trim();
									ussdXmlRPCRequestBean.setXmlResponse(xmlResponse);;
								}
								
							}

						}

					}

				}
			}
				catch (JsonParseException e) {
					LOGGER.debug("Exception in parsing of JSON. Read the server logs for stack trace.",e);
					throw new Exception();
				} catch (IOException e) {
					LOGGER.debug("IOException during parsing of JSON. Read the server logs for stack trace.",e);
					throw new Exception();
				}catch (Exception e) {
					LOGGER.debug("Exception during parsing of JSON. Read the server logs for stack trace.",e);
					throw new Exception();
				}
	
			UssdZteXMLResponseBean res = new UssdZteXMLResponseBean();
			DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
			 Date date = new Date();
			res.setTransactionId(dateFormat.format(date).toString());
			res.setTransactionTime(transactionTime);
			 boolean returnValue = validateRequest(ussdXmlRPCRequestBean,res);
			 if(returnValue){
				 executeFaultResponse(res, exchange);
				 return;
			 }
			  FDPCircle fdpCircle = CircleCodeFinder.getFDPCircleByMsisdn(msisdn,
					ApplicationConfigUtil.getApplicationConfigCache());
			String serviceType = "http";
			final String requestId = SMPPUtil.generateRequestId(serviceType);
					
			FDPRequestImpl fdpRequestImpl = null;
			//String sessionId = ussdHttpRequest.getSessionId();
			//String requestType=ussdHttpRequest.getType();
			//if (requestType.equalsIgnoreCase(BusinessConstants.HTTP_REQUEST_TYPE_PULL)) {
			
				/*LOGGER.debug("Getting value from cache for session id : {} service {}", new Object[] { sessionId,
						serviceType });
			*/
			FDPSMPPRequestImpl fdpussdsmscRequestImpl = null;
			
			//FDPRequestBag fdpRequestBag = new FDPRequestBag(sessionId);
			FDPRequestBag fdpRequestBag = new FDPRequestBag(msisdn);
			fdpRequestImpl = (FDPRequestImpl) fdpRequestCacheForUSSD.getValue(fdpRequestBag);
			if (fdpRequestImpl != null) {
				if (fdpRequestImpl instanceof FDPRequest) {
					
					fdpussdsmscRequestImpl = (FDPSMPPRequestImpl) fdpRequestImpl;
					//LOGGER.debug("Found value from cache for session id : {}", sessionId);
				} 
			}else {
				fdpussdsmscRequestImpl = new FDPSMPPRequestImpl();
				
				LOGGER.debug("Creating new request impl");
		}
			fdpussdsmscRequestImpl.setRequestId(requestId);
			fdpussdsmscRequestImpl.setCircle(fdpCircle);
			fdpussdsmscRequestImpl.setSessionId(transactionId);
			final CircleConfigParamDTO circleConfigParamDTO = RequestUtil.populateCircleConfigParamDTO(msisdn,
					fdpCircle);
			fdpussdsmscRequestImpl.setOriginNodeType(circleConfigParamDTO.getOriginNodeType());
			fdpussdsmscRequestImpl.setSubscriberNumberNAI(circleConfigParamDTO.getSubscriberNumberNAI());
			fdpussdsmscRequestImpl.setOriginHostName(circleConfigParamDTO.getOriginHostName());
			fdpussdsmscRequestImpl.setSubscriberNumber(circleConfigParamDTO.getSubscriberNumber());
			fdpussdsmscRequestImpl.setIncomingSubscriberNumber(circleConfigParamDTO.getIncomingSubscriberNumber());
			
				fdpussdsmscRequestImpl.setChannel(ChannelType.USSD);
				
			fdpussdsmscRequestImpl.setRequestString(new FDPUSSDRequestStringImpl(ussdRequestString));
			
				final FDPResponse fdpResponse = fdpDynamicMenu.executeDynamicMenu(fdpussdsmscRequestImpl);
				//Response res = new Response();
				if(fdpResponse!=null){
					
					
					List<ResponseMessage> responseString=fdpResponse.getResponseString();
					Iterator it=responseString.iterator();
					if(it.hasNext()){
					 ResponseMessage message=(ResponseMessage)it.next();
					 String responseMessage =message.getCurrDisplayText(DisplayArea.COMPLETE);
					 if(responseMessage!=null && !responseMessage.isEmpty()){
						 res.setUssdResponseString(message.getCurrDisplayText(DisplayArea.COMPLETE));
						 List<TLVOptions> sessionValue=message.getTLVOptions();
						 if(sessionValue.contains(TLVOptions.SESSION_TERMINATE)){
							 res.setAction("end");
							 final FDPRequestBag fdpRequest = new FDPRequestBag(msisdn);
								fdpRequestCacheForUSSD.removeKey(fdpRequest);
						 }
						 else{
							 res.setAction("request");
						 }
					 }
					}
					 else
						 res.setUssdResponseString("The Input is not valid.");
					res.setAction("request");
				//	res.setMsisdn(ussdHttpRequest.getMsisdn());
				}
				 else{
					 res.setUssdResponseString("The Input is not valid.");
				res.setAction("request");
				 }
				final List<ResponseMessage> messageList = fdpResponse.getResponseString();
				boolean flushSession = true;
				for (final ResponseMessage message : messageList) {
					flushSession = flushSession && message.getTLVOptions().contains(TLVOptions.SESSION_TERMINATE);
				}
				final String systemType = fdpResponse.getSystemType();
				if(systemType !=null){					
					flushSession=true;					
				}
				executeResponse(res, exchange);
							
	
		}catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param xml The XML in form of String.
	 * @return JSON The string of JSON Object.
	 */
	public static String processXMLRequest(String xml){

		JSONObject xmlJSONObj = XML.toJSONObject(xml);
		return xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);

	}
	
private void executeResponse(UssdZteXMLResponseBean res, Exchange exchange){
		
		Map<String, String> parameterMap = new HashMap<String, String>();
		final Writer finalRequestXml = new StringWriter();
		
		Message out = exchange.getOut();
		exchange.setProperty(Exchange.CONTENT_TYPE, "text/xml");
			
		try{

		parameterMap.put(USSDXmlZTEConstants.TRANSACTION_ID ,res.getTransactionId());
		parameterMap.put(USSDXmlZTEConstants.TRANSACTION_TIME, res.getTransactionTime());
		parameterMap.put(USSDXmlZTEConstants.USSD_RESPONSE_STRING ,res.getUssdResponseString());
		parameterMap.put(USSDXmlZTEConstants.ACTION ,res.getAction());
		
		Template template = new Template("$", new StringReader(XMLResponseConstants.XML_RESPONSE), new Configuration());
		template.process(parameterMap, finalRequestXml);

	
		
		} catch(Exception e) {
			e.printStackTrace();	
		}
		
		out.setBody(finalRequestXml.toString());
	
	}

private boolean validateRequest(UssdZteXMLRequestBean ussdZteXMLRequestBean,UssdZteXMLResponseBean res ) {
	
	boolean returnVal = false;

	if(ussdZteXMLRequestBean.getTransactionId().isEmpty() || ussdZteXMLRequestBean.getTransactionTime().isEmpty()
			|| ussdZteXMLRequestBean.getMsisdn().isEmpty() || ussdZteXMLRequestBean.getUssdRequestString().isEmpty()
			|| ussdZteXMLRequestBean.getXmlResponse().isEmpty()){
		res.setFaultCode(USSDXmlZTEConstants.MANDATORY_FIELD_MISSING_CODE);
		res.setFaultString(USSDXmlZTEConstants.MANDATORY_FIELD_MISSING);
		returnVal = true;
		
	}
	else
	{
		
		// MOBILe No. validation
		
		Integer msisdnLength = Integer.parseInt(applicationConfigCache.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP,
				"MSISDN_NUMBER_LEN")).toString());
		String coutryCodeString = applicationConfigCache.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP,
				"COUNTRY_CODE")).toString();
		Integer countryCodeLength = coutryCodeString.length();
		
		
		String mobNo = ussdZteXMLRequestBean.getMsisdn();
		
		if(mobNo.length() == msisdnLength+countryCodeLength && mobNo.startsWith(coutryCodeString)){
			res.setFaultCode(USSDXmlZTEConstants.APPLICATION_BUSY_CODE);
			res.setFaultString(USSDXmlZTEConstants.APPLICATION_BUSY);
			returnVal = true;
		}
		else if(mobNo.length() == msisdnLength){
			res.setFaultCode(USSDXmlZTEConstants.APPLICATION_BUSY_CODE);
			res.setFaultString(USSDXmlZTEConstants.APPLICATION_BUSY);
			returnVal = true;
		}
		/*else{
			res.setFaultCode(USSDXmlZTEConstants.WRONG_NUMBER_OF_DIGITS);
			res.setFaultString(USSDXmlZTEConstants.WRONG_NUMBER_FORMAT_RESPONSE);
			return returnVal = false;				
		}*/
		
		/*try{
			Long mobNoL = Long.parseLong(mobNo);			
			returnVal = true;
		}catch(NumberFormatException e){
			res.setFaultCode(USSDXmlZTEConstants.WRONG_NUMBER_FORMAT);
			res.setFaultString(USSDXmlZTEConstants.WRONG_NUMBER_FORMAT_RESPONSE);
			return returnVal = false;
		}*/
	}
		
	return returnVal;
}

private void executeFaultResponse(UssdZteXMLResponseBean res, Exchange exchange){
	
	
	Map<String, String> parameterMap = new HashMap<String, String>();
	final Writer finalRequestXml = new StringWriter();
	
	Message out = exchange.getOut();
	exchange.setProperty(Exchange.CONTENT_TYPE, "text/xml");
	//Boolean EOS = java.lang.Boolean.parseBoolean("FALSE");
	
	try{

	parameterMap.put(USSDXmlZTEConstants.TRANSACTION_ID ,res.getTransactionId());
	parameterMap.put(USSDXmlZTEConstants.TRANSACTION_TIME, res.getTransactionTime());
	parameterMap.put(USSDXmlZTEConstants.FAULT_CODE ,res.getFaultCode());
	parameterMap.put(USSDXmlZTEConstants.FAULT_STRING ,res.getFaultString());
	
	Template template = new Template("$", new StringReader(XMLResponseConstants.XML_FAULT_RESPONSE), new Configuration());
	template.process(parameterMap, finalRequestXml);


	
	} catch(Exception e) {
		e.printStackTrace();	
	}
	
	out.setBody(finalRequestXml.toString());

}

	
}
	
