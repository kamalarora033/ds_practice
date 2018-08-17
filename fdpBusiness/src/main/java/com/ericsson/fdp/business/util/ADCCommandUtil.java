package com.ericsson.fdp.business.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This is a utility class that deals with ADC commands.
 * 
 * @author Ericsson
 * 
 */
public class ADCCommandUtil {

	private static final Logger LOGGER = LoggerFactory.getLogger(ADCCommandUtil.class);
	/**
	 * Instantiates a new ADC command util.
	 */
	private ADCCommandUtil() {

	}


	public static Map<String, CommandParam> fromADCXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		try{
			return convertNodesFromXml(outputParam);
		}catch (final ParserConfigurationException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}	
	}
	
	public static Map<String, CommandParam> convertNodesFromXml(String xml) throws ParserConfigurationException{
		Map<String, CommandParam> outputParams = new LinkedHashMap<>();
		try(InputStream is = new ByteArrayInputStream(xml.getBytes())){
		    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    dbf.setValidating(false);
		    dbf.setSchema(null);
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    Document document = db.parse(is);
		    outputParams =createMap(document.getDocumentElement(),outputParams);
		}catch ( IllegalArgumentException | SAXException | IOException e) {
			LOGGER.error("Error Occured while reading Loyalty input xml", e);
		}
	    return outputParams;
	}
	
	public static Map<String, CommandParam> createMap(Node node,
			Map<String, CommandParam> outputParams) {
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node currentNode = nodeList.item(i);
			if (null == currentNode.getNodeValue()
					&& currentNode.getNodeType() == Node.ELEMENT_NODE
					&& currentNode.getAttributes().getLength() > 0) {
				NamedNodeMap attribute = currentNode.getAttributes();
				if (attribute.getNamedItem("name")==null || !attribute.getNamedItem("name").getNodeValue()
						.equals("MSISDN")) {
					for (int j = 0; j < currentNode.getAttributes().getLength(); j++) {
						Node attributeValue = attribute.item(j);
						final CommandParamOutput commandParamOutput = new CommandParamOutput();
						commandParamOutput.setValue(attributeValue
								.getNodeValue());
						commandParamOutput.setPrimitiveValue(Primitives.STRING);
						commandParamOutput
								.setName(attributeValue.getNodeName());
						commandParamOutput
								.setType(CommandParameterType.PRIMITIVE);
						outputParams.put(attributeValue.getNodeName(),
								commandParamOutput);
					}
				}
			}
			if (null == currentNode.getNodeValue()
					&& null != currentNode.getFirstChild()
					&& currentNode.getNodeType() == Node.ELEMENT_NODE) {
				createMap(currentNode, outputParams);
			}
		}
		if(outputParams!=null && outputParams.containsKey("value")){
		CommandParamOutput listData=(CommandParamOutput) outputParams.get("value");
		String[] array=listData.getValue().toString().split(",");
		int count=0;
		for(String data:array){
			final CommandParamOutput commandParamOutput = new CommandParamOutput();
			commandParamOutput.setValue(data);
			commandParamOutput.setPrimitiveValue(Primitives.STRING);
			commandParamOutput
					.setName("value."+count+".0");
			commandParamOutput
					.setType(CommandParameterType.PRIMITIVE);
			outputParams.put("value."+count+".0",
					commandParamOutput);
			count++;
		}
		}
		return outputParams;
	}

		

	public static String toADCXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("?").append(toParametersXmlFormat(command.getInputParam()));
		return xmlFormat.toString();
	}

	public static String toParametersXmlFormat(final List<CommandParam> inputParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder(256);
		for (final CommandParam commandParam : inputParam) {
			if(commandParam.getName() != null)
			xmlFormat.append(toXmlfromPrimitiveParam(commandParam));
		}
		return xmlFormat.toString();
	}

	private static String toXmlfromPrimitiveParam(final CommandParam commandParam) {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("&").append(commandParam.getName()).append("=").append(commandParam.getValue().toString());
		return xmlFormat.toString();
	}
	
	
	public static String toADCParametersXmlFormat(final List<CommandParam> inputParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		for (final CommandParam commandParam : inputParam) {
			xmlFormat.append(CommandParamUtil.toXmlForParam(commandParam));
		}
		return xmlFormat.toString();
	}

	
	public static CommandExecutionStatus checkForADCCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.RESPONSE_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandParam responseCodeParam = fdpCommand.getOutputParam("code");
		if (responseCodeParam != null) {
			isFailure = CommandUtil.checkResponseCode(configCache, responseCodeParam.getValue(),
					fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode(Integer.parseInt(responseCodeParam.getValue().toString()));
			if (isFailure != null) {
				commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
				//setting write flag to true
				if(isFailure.getResultCodeType()==1)
					((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
			}
		}
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		//setting write flag to true
		if(isFailure==null)
			((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
		return commandExecutionStatus;
	}
	
	

}
