package com.ericsson.fdp.business.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This is a utility class that deals with LOYALTY commands.
 * 
 * @author Ericsson
 * 
 */
public class LoyaltyCommandUtil {

	/**
	 * Instantiates a new Lolyalty command util.
	 */
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LoyaltyCommandUtil.class);

	private static final String CDATA_OPENING_TAG ="<!\\[CDATA\\[";
	private static final String CDATA_END_TAG ="]]>";
	private static final String UTF="UTF-8";
	
	private LoyaltyCommandUtil() {}

	public static String toLoyaltyXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		return createSOAPRequest(command);
	}

	public static String toLoyaltyParametersXmlFormat(final List<CommandParam> inputParam)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		for (final CommandParam commandParam : inputParam) {
			xmlFormat.append(CommandParamUtil.toXmlForParamLoyalty(commandParam));
		}
		return xmlFormat.toString();
	}
	/**
	 * This method is used to create a soap client request envelope
	 * @param command
	 * @return 
	 * @throws EvaluationFailedException
	 */
	public static String createSOAPRequest(final FDPCommand command) throws ExecutionFailedException {
		SOAPMessage soapMessage;
		try {  	
				String url = PropertyUtils.getProperty("LCMS_SOAP_URL");
				String xmlns = PropertyUtils.getProperty("LCMS_SOAP_NAMESPACE");
				MessageFactory messageFactory = MessageFactory.newInstance();
				soapMessage = messageFactory.createMessage();
				SOAPPart soapPart = soapMessage.getSOAPPart();
				SOAPEnvelope envelope = soapPart.getEnvelope();
				envelope.addNamespaceDeclaration(xmlns, url);
				SOAPBody soapBody = envelope.getBody();
				SOAPElement soapBodyElem = soapBody.addChildElement(PropertyUtils.getProperty("LCMS_SOAP_SERVICE_REQUEST"), xmlns);
				SOAPElement soapBodyElem1 = soapBodyElem.addChildElement(PropertyUtils.getProperty("LCMS_SOAP_INPUT"), xmlns);
				CDATASection cdata = soapBodyElem1.getOwnerDocument().createCDATASection(toLoyaltyParametersXmlFormat(command.getInputParam()));
				soapBodyElem1.appendChild(cdata);
				soapMessage.saveChanges();
				ByteArrayOutputStream outputstream = new ByteArrayOutputStream();
				soapMessage.writeTo(outputstream);
			return new String(outputstream.toByteArray(), UTF).replaceAll(CDATA_OPENING_TAG, "").replaceAll(CDATA_END_TAG,"");
		} catch (SOAPException | IOException e) {
			LOGGER.error("Error Occured while parsing Loyalty input xml", e);
		}
		return "";
	}

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for
	 * Loyalty parameters.
	 * 
	 * @param outputXmlAsString
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException 
	 * @throws Exception 
	 */
	public static synchronized Map<String, CommandParam> fromLoyaltyXmlToParameters(String outputParam) throws EvaluationFailedException {
			try{
				//return convertNodesFromXml(outputParam);
				return convertParameterFromXML(outputParam);
			}catch (final Exception e) {
				throw new EvaluationFailedException("The resposne xml is not valid.", e);
			}		
	}
	
	
	public static Map<String, CommandParam> convertNodesFromXml(String xml) throws ParserConfigurationException{
		Map<String, CommandParam> outputParams = new LinkedHashMap<>();
		try{
		    InputStream is = new ByteArrayInputStream(xml.getBytes());
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    dbf.setValidating(false);
		    dbf.setSchema(null);
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    Document document = db.parse(is);
		    outputParams =createMap(document.getDocumentElement(),outputParams);
		}catch ( SAXException | IOException e) {
			LOGGER.error("Error Occured while reading Loyalty input xml", e);
		}
	    return outputParams;
	}
	
	/**
	 * This method will convert xml response into parameters 
	 * @param xml
	 * @return
	 */
	private static Map<String, CommandParam> convertParameterFromXML(String xml) {
		Map<String, CommandParam> outputParams = new LinkedHashMap<>();
		try {
			
			MessageFactory factory = MessageFactory.newInstance();
		    SOAPMessage message = factory.createMessage(
		            new MimeHeaders(),
		            new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
	
		    SOAPBody body = message.getSOAPBody();
		    NodeList returnList = body.getElementsByTagName("responseOutput");
		    
		    if (returnList.item(0).getChildNodes().getLength() > 0)
		    	processResponseOutput("responseoutput.responseoutput.",returnList.item(0).getChildNodes().item(0).getChildNodes(), outputParams);
		    if (returnList.item(0).getChildNodes().getLength() > 1)
		    	processRowset("responseoutput.rowset.",returnList.item(0).getChildNodes().item(1).getChildNodes(), outputParams);
		   
		} catch (SOAPException | IOException e) {
			LOGGER.error("Error Occured while reading Loyalty response xml", e);
		}
		 return outputParams;
	}

	public static Map<String, CommandParam> createMap(Node node,Map<String, CommandParam> outputParams) {
			NodeList nodeList = node.getChildNodes();
		    for (int i = 0; i < nodeList.getLength(); i++) {
		        Node currentNode = nodeList.item(i);
		        if (null == currentNode.getNodeValue() && null != currentNode.getFirstChild() && currentNode.getNodeType() == Node.ELEMENT_NODE) {
		        	createMap(currentNode,outputParams);
		        } else if (null !=currentNode.getNodeValue() && currentNode.getNodeType() == Node.TEXT_NODE && !currentNode.getNodeValue().trim().isEmpty()) {
	        	final CommandParamOutput commandParamOutput = new CommandParamOutput();				
	        	commandParamOutput.setValue(currentNode.getNodeValue());
				commandParamOutput.setPrimitiveValue(Primitives.STRING);
				commandParamOutput.setName(currentNode.getParentNode().getNodeName());
				commandParamOutput.setType(CommandParameterType.PRIMITIVE);
				outputParams.put(currentNode.getParentNode().getNodeName().toLowerCase(), commandParamOutput);
	        }
		 }
	    return outputParams;
	}

	/**
	 * This method is used to check if the Loyalty command executed successfully
	 * or failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if the status could not be evaluated.
	 */
	public static CommandExecutionStatus checkForLoyaltyCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure;
		final CommandParam faultCodeParam = fdpCommand.getOutputParams().get(FDPCommandConstants.LOYALTY_FAULT_CODE_PATH.toLowerCase());
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		if (faultCodeParam != null) {
			isFailure = CommandUtil.checkFaultCode(configCache, faultCodeParam.getValue(), fdpCommand.getSystem())!=null ? 
					CommandUtil.checkFaultCode(configCache, faultCodeParam.getValue(), fdpCommand.getSystem()) : 
						CommandUtil.checkResponseCode(configCache, faultCodeParam.getValue(),fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode(Integer.parseInt((String) faultCodeParam.getValue()));
			if(null!=fdpCommand.getOutputParams().get(FDPCommandConstants.LOYALTY_FAULT_CODE_DESC_PATH.toLowerCase()))
				commandExecutionStatus.setDescription(fdpCommand.getOutputParams().get(FDPCommandConstants.LOYALTY_FAULT_CODE_DESC_PATH.toLowerCase()).toString());
			else
				if (isFailure != null) 
					commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
			if(isFailure.getResultCodeType()==1)
				((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
			
		} else {
				isFailure = new FDPResultCodesDTO();
				isFailure.setIsRollback(Status.FAILURE);
				commandExecutionStatus.setErrorType(ErrorTypes.RESPONSE_CODE.name());
				((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
		}
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		return commandExecutionStatus;
	}
	
	/**
	 * This method will process ResponseOutput node and store it into output map
	 * @param parentNode
	 * @param innerResultList
	 * @param outputParams
	 */
	private static void processResponseOutput(String parentNode, NodeList innerResultList, Map<String, CommandParam> outputParams) {
		 for (int count = 0; count < innerResultList.getLength(); count++) {
			outputParams.put(parentNode + innerResultList.item(count).getNodeName().trim().toLowerCase(), getcommandParamOutput(innerResultList, count));
	     }
	}
	
	/**
	 * This method will process Rowset node and store it into output map
	 * @param parentNode
	 * @param innerResultList
	 * @param outputParams
	 */
	private static void processRowset(String parentNode, NodeList innerResultList, Map<String, CommandParam> outputParams) {
		if (innerResultList != null) {
			for (int childCount = 0; childCount < innerResultList.getLength(); childCount++) {
				NodeList rowList = innerResultList.item(childCount).getChildNodes();
				if (rowList != null) {
					for (int rowCount = 0; rowCount < rowList.getLength(); rowCount++) {
						outputParams.put(parentNode + childCount + "." + rowList.item(rowCount).getNodeName().trim().toLowerCase(), getcommandParamOutput(rowList, rowCount));
					}
				}
			}
			
		}	
	}
	
	/**
	 * This method will construct the CommandParamOutput
	 * @param node
	 * @param count
	 * @return the commandParamOutput
	 */
	private static CommandParamOutput getcommandParamOutput(NodeList node, int count) {
		final CommandParamOutput commandParamOutput = new CommandParamOutput();
		commandParamOutput.setValue(node.item(count).getTextContent().trim());
		commandParamOutput.setPrimitiveValue(Primitives.STRING);
		commandParamOutput.setName(node.item(count).getNodeName().trim().toLowerCase());
		commandParamOutput.setType(CommandParameterType.PRIMITIVE);
		return commandParamOutput;
	}
}
