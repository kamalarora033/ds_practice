package com.ericsson.fdp.business.route.processor;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.cdi.CdiCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPESFConfigDTO;

/**
 * The Class AbilityRequestProcessor is used to create routes for Ability
 * request which comes from external systems.
 * 
 * @author Ericsson
 */
public class ESFRequestProcessor implements Processor{


	
	private static final String COMMA_DELIMITER = ",";
	
	private static final String NEW_LINE_SEPARATOR = "\n";
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(AbilityRequestProcessor.class);

	/** The context. */
	private CdiCamelContext context;
	
	@Override
	public void process(final Exchange exchange) throws Exception {

		String inputXML = "";
		String outputXML = "";
		
		try {
			
			
			FDPESFConfigDTO fdpESFConfigDTO = new FDPESFConfigDTO();
			
			
			LOGGER.debug("Invoking ESFRequestProcessor().... with adapter request ");
			final Message in = exchange.getIn();
			inputXML = in.getBody(String.class);
			
			fdpESFConfigDTO = (FDPESFConfigDTO)exchange.getProperty("ABILITY_OBJ");
			String endPointURL = getESFEndpointURLForCircle(fdpESFConfigDTO);
			
			System.out.println("endPointURL-------->>"+	endPointURL);
			System.out.println("inputXML-------->>"+	inputXML);
			
			context = ApplicationConfigUtil.getCdiCamelContextProvider().getContext();
			Endpoint endpoint = context.getEndpoint(endPointURL);
			
			final Exchange exchangeURL = endpoint.createExchange();
			exchange.setPattern(ExchangePattern.InOut);
			final Message inURL = exchangeURL.getIn();
			
			inURL.setBody(inputXML.toString());
			final Producer producer = endpoint.createProducer();
			producer.process(exchangeURL);	
			
			
			final Message out = exchangeURL.getOut();
			outputXML = out.getBody(String.class);
			System.out.println("outputXML-------->>"+	outputXML);
		
			writeCVS(inputXML, outputXML);
			
		} catch (final Exception e) {
			writeCSVFileFromResponseXML(inputXML);
			LOGGER.error(e.getMessage(), e);
		}

	}
	
			
	/**
	 * This method is used to return AIR URL.
	 * 
	 * @param fdpAIRConfigDTO
	 *            the fdp air config dto
	 * @return AIR Url
	 */
	private String getESFEndpointURLForCircle(final FDPESFConfigDTO fdpESFConfigDTO) {
		final String esfUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpESFConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpESFConfigDTO.getPort()+"/"+ fdpESFConfigDTO.getContextPath()+ BusinessConstants.QUERY_STRING_SEPARATOR
				+ BusinessConstants.HTTP_CLIENT_SO_TIMEOUT + BusinessConstants.EQUALS + fdpESFConfigDTO.getResponseTimeout();
		LOGGER.debug("Ability Url:" + esfUrl);
		return esfUrl;
	}
	
	
	
	private void writeCVS(String inputXML, String outputXML){
		boolean isSuccess = false;
		
		isSuccess = reponseStatus(outputXML);
		
		if(!isSuccess){
			writeCSVFileFromResponseXML(inputXML);
		}
	}
	
	
	private boolean reponseStatus(String outputXML){
		 boolean isSuccess = false;
		try {
			MessageFactory factory = MessageFactory.newInstance();
		
		    SOAPMessage message = factory.createMessage(
		            new MimeHeaders(),
		            new ByteArrayInputStream(outputXML.getBytes(Charset.forName("UTF-8"))));
	
		    SOAPBody body = message.getSOAPBody();
		    NodeList returnList = body.getElementsByTagName("API_OUTPUT");

		    for (int k = 0; k < returnList.getLength(); k++) {
		        NodeList innerResultList = returnList.item(k).getChildNodes();
		        for (int l = 0; l < innerResultList.getLength(); l++) {
		            if (innerResultList.item(l).getNodeName().equalsIgnoreCase("REQUEST_STATUS")) {
		                isSuccess = Integer.valueOf(innerResultList.item(l).getTextContent().trim()) == 0 ? true : false;
		            }
		        }
		    }
		    
		} catch (SOAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return isSuccess;
	}
	
	
	private void writeCSVFileFromResponseXML(String xml){
		try {
			boolean isSuccess = true;
			MessageFactory factory = MessageFactory.newInstance();
		    SOAPMessage message = factory.createMessage(
		            new MimeHeaders(),
		            new ByteArrayInputStream(xml.getBytes(Charset
		                    .forName("UTF-8"))));
		
		    SOAPBody body = message.getSOAPBody();
		
		    
		    String cisTransactionNo = "";
		    String externalApplication = "";
		    String subscriberMSISDN = "";
		    String offerOperation = "";
		    String cisOfferCode = "";
		    String activationDate = "";
		    String expiryDate = "";
		    String offerCharge = "";
		    String orderStatus = "";

		    if (isSuccess) {
		        NodeList list = body.getElementsByTagName("EVENT");
		
		        for (int i = 0; i < list.getLength(); i++) {
		            NodeList innerList = list.item(i).getChildNodes();
		
		            for (int j = 0; j < innerList.getLength(); j++) {
		            	if(innerList.item(j).getAttributes().getNamedItem("EXTERNAL_REFERENCE") !=null ){
		            		cisTransactionNo = innerList.item(j).getAttributes().getNamedItem("EXTERNAL_REFERENCE").getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem("EXTERNAL_APPLICATION") !=null ){
		            		externalApplication = innerList.item(j).getAttributes().getNamedItem("EXTERNAL_APPLICATION").getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem("ENTITY_ID") !=null ){
		            		subscriberMSISDN = innerList.item(j).getAttributes().getNamedItem("ENTITY_ID").getNodeValue();
		            	}
		            	
		            	if(innerList.item(j).getAttributes().getNamedItem("SUBSCRIPTION_FLAG") !=null ){
		            		offerOperation = innerList.item(j).getAttributes().getNamedItem("SUBSCRIPTION_FLAG").getNodeValue();
		            	}
		                
		            	if(innerList.item(j).getAttributes().getNamedItem("OFFER_CODE") !=null ){
		            		cisOfferCode = innerList.item(j).getAttributes().getNamedItem("OFFER_CODE").getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem("ACTIVATION_DATE") !=null ){
		            		activationDate = innerList.item(j).getAttributes().getNamedItem("ACTIVATION_DATE").getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem("EXPIRY_DATE") !=null ){
		            		expiryDate = innerList.item(j).getAttributes().getNamedItem("EXPIRY_DATE").getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem("OFFER_CHARGE") !=null ){
		            		offerCharge = innerList.item(j).getAttributes().getNamedItem("OFFER_CHARGE").getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem("ORDER_STATUS") !=null ){
		            		orderStatus = innerList.item(j).getAttributes().getNamedItem("ORDER_STATUS").getNodeValue();
		            	}
		            	System.out.println(orderStatus);
		            }
		        }
		        FileWriter fileWriter = null;
		        try {

			        String fileName = System.getProperty("user.home")+File.separator+"ability.csv";
			       
			        System.out.println(fileName);
		        	fileWriter = new FileWriter(fileName, true);
		        	fileWriter.append(cisTransactionNo);
		        	fileWriter.append(COMMA_DELIMITER);
		        	fileWriter.append(externalApplication);
		        	fileWriter.append(COMMA_DELIMITER);
		        	fileWriter.append(subscriberMSISDN);
		        	fileWriter.append(COMMA_DELIMITER);
		        	fileWriter.append(offerOperation);
		        	fileWriter.append(COMMA_DELIMITER);
		        	fileWriter.append(cisOfferCode);
		        	fileWriter.append(COMMA_DELIMITER);
		        	fileWriter.append(activationDate);
		        	fileWriter.append(COMMA_DELIMITER);
		        	fileWriter.append(expiryDate);
		        	fileWriter.append(COMMA_DELIMITER);
		        	fileWriter.append(offerCharge);
		        	fileWriter.append(COMMA_DELIMITER);
		        	fileWriter.append(orderStatus);
		        	fileWriter.append(NEW_LINE_SEPARATOR);

		        	System.out.println("CSV file was created successfully !!!");
		        } catch (Exception e) {
		        	System.out.println("Error in CsvFileWriter !!!");
		        	e.printStackTrace();
		        } finally {
		        	try {
		        		fileWriter.flush();
		        		fileWriter.close();
		        	} catch (IOException e) {
		        		System.out.println("Error while flushing/closing fileWriter !!!");
		        		e.printStackTrace();
		        	}
		        }

		    }	
		} catch (SOAPException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	
		
	}
	
	
}
