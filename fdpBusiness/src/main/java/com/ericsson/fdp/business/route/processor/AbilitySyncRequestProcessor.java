package com.ericsson.fdp.business.route.processor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import com.ericsson.fdp.business.constants.BusinessConstants;

/**
 * The Class AbilityRequestProcessor is used to create routes for Ability request which comes from external systems.
 * 
 * @author Ericsson
 */
public class AbilitySyncRequestProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbilitySyncRequestProcessor.class);


    @Override
    public void process(final Exchange exchange) throws Exception {
        String outputXML = "";
        final Message in = exchange.getIn();
        outputXML = in.getBody(String.class);
        LOGGER.info("Ability Sync call : outputXML-------->>" + outputXML);
        if (reponseStatus(outputXML)) {
            exchange.getOut().setHeader(BusinessConstants.RESPONSE_CODE, "0");
        } else {
            exchange.getOut().setHeader(BusinessConstants.RESPONSE_CODE, getErrorCode(outputXML));
        }
        exchange.getOut().setBody(outputXML);
        LOGGER.debug("Ability Sync call : outputXML-------->>" + outputXML);
//        LOGGER.info("Error Occured Request " + exchange.getIn().getBody(String.class));
    }


    private boolean reponseStatus(String outputXML) {
        boolean isSuccess = false;
        try {
            MessageFactory factory = MessageFactory.newInstance();

            SOAPMessage message = factory.createMessage(new MimeHeaders(),
                    new ByteArrayInputStream(outputXML.getBytes(Charset.forName("UTF-8"))));

            SOAPBody body = message.getSOAPBody();
            NodeList returnList = body.getElementsByTagName("API_OUTPUT");

            for (int k = 0; k < returnList.getLength(); k++) {
                NodeList innerResultList = returnList.item(k).getChildNodes();
                for (int l = 0; l < innerResultList.getLength(); l++) {
                    if ("REQUEST_STATUS".equalsIgnoreCase(innerResultList.item(l).getNodeName())) {
                        isSuccess = Integer.valueOf(innerResultList.item(l).getTextContent().trim()) == 0 ? true : false;
                    }
                }
            }

        } catch (SOAPException | IOException e) {
            LOGGER.error("Error in reponseStatus", e);
        }
        return isSuccess;
    }


    private int getErrorCode(String outputXML) {
        int status = 0;
        try {
            MessageFactory factory = MessageFactory.newInstance();
            SOAPMessage message = factory.createMessage(new MimeHeaders(),
                    new ByteArrayInputStream(outputXML.getBytes(Charset.forName("UTF-8"))));
            SOAPBody body = message.getSOAPBody();
            NodeList returnList = body.getElementsByTagName("API_OUTPUT");
            for (int k = 0; k < returnList.getLength(); k++) {
                NodeList innerResultList = returnList.item(k).getChildNodes();
                for (int l = 0; l < innerResultList.getLength(); l++) {
                    if ("RESPONSE_ERROR_CODE".equalsIgnoreCase(innerResultList.item(l).getNodeName())) {
                        status = Integer.valueOf(innerResultList.item(l).getTextContent().trim());
                    }
                }
            }
        } catch (SOAPException | IOException e) {
            LOGGER.error("Error in getErrorCode", e);
        }
        return status;
    }
}
