package com.ericsson.fdp.business.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.business.enums.Command;
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
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * This is a utility class that deals with Ability commands.
 * 
 * @author Ericsson
 * 
 */
public class AbilityCommandUtil {

	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
    private static final String ABILITY_DATE_PATTERN = "YYYYMMdd";// "dd/MM/YYYY";
	private static final Logger LOGGER = LoggerFactory.getLogger(AbilityCommandUtil.class);
	private static final String ACTIVATION_DATE = "ACTIVATION_DATE";
	private static final String EXPIRY_DATE = "EXPIRY_DATE";
	/**
	 * Instantiates a new Ability command util.
	 */
	private AbilityCommandUtil() {

	}

	/**
	 * This method is used to create string representation of xml for an Ability
	 * command. The supported version is 5.0 for Ability commands. The
	 * implementation will be changed to support a different version.
	 * 
	 * @param command
	 *            The command for which the xml representation is to be created.
	 * @return The string representation of the command.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
    public static String toAbilityXmlFormat(final FDPCommand command, final FDPRequest fdpRequest) throws ExecutionFailedException {
        final StringBuilder xmlFormat = new StringBuilder();

        if (Command.VAS_ADD_REMOVE.getCommandDisplayName().equals(command.getCommandDisplayName())) {
            for (final CommandParam commandParam : command.getInputParam()) {
                xmlFormat.append(convertAbilityParamToXml(commandParam, fdpRequest));
            }
        } else {
            xmlFormat.append("<EVENT>").append("<REQUEST");
            for (final CommandParam commandParam : command.getInputParam()) {
                if ("EXTERNAL_SYSTEMS_LOG_REFERNCE".equalsIgnoreCase(commandParam.getName())
                        || commandParam.getName().equalsIgnoreCase(ACTIVATION_DATE) || commandParam.getName().equalsIgnoreCase(EXPIRY_DATE)
                        || "AMT_DATA_TRANSDT".equalsIgnoreCase(commandParam.getName())) {
                    appendDateParamToXML(xmlFormat, commandParam);
                } else {
                    xmlFormat.append(" ").append(commandParam.getName()).append("=").append("\"" + commandParam.getValue() + "\"");
                }
            }
            xmlFormat.append(" />").append("</EVENT>");
        }
        return xmlFormat.toString();
    }
	
	public static String convertAbilityParamToXml(final CommandParam commandParam, final FDPRequest fdpRequest){
		final StringBuilder xmlFormat = new StringBuilder();
		boolean haveStructChild = false;
		
		switch (commandParam.getType()) {
		case STRUCT:
			xmlFormat.append("\n").append("<").append(commandParam.getName()).append("");
			for(CommandParam param : commandParam.getChilderen()){
				haveStructChild = haveStructChild || CommandParameterType.STRUCT.equals(param.getType());
				if(CommandParameterType.PRIMITIVE.equals(param.getType())){
					xmlFormat.append(" ");
					xmlFormat.append(convertAbilityParamToXml(param, fdpRequest));
				}
			}
			if(haveStructChild)
				xmlFormat.append(">");
			for(CommandParam param : commandParam.getChilderen()){
				if(CommandParameterType.STRUCT.equals(param.getType())){
					xmlFormat.append(convertAbilityParamToXml(param, fdpRequest));
				}
			}
			if(haveStructChild)
				xmlFormat.append("<").append("/").append(commandParam.getName()).append(">").append("\n");
			else
				xmlFormat.append(" /").append(">");
			break;
		case PRIMITIVE:
			Object value = commandParam.getValue();
            if (Primitives.DATETIME.equals(commandParam.getPrimitiveValue()) && value != null) {
                SimpleDateFormat sf = new SimpleDateFormat(FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
                Date date;
                try {
                    date = sf.parse(value.toString());
                    sf = new SimpleDateFormat(ABILITY_DATE_PATTERN);
                    sf.format(date.getTime());
                    value = sf.format(date.getTime());
                } catch (Exception e) {
                    LOGGER.error("Error Occurred at convertAbilityParamToXml", e);
                }
            }
            if (value != null)
                xmlFormat.append(" ").append(commandParam.getName()).append("=").append("\"" + value.toString() + "\"");
			break; 
		default:
			break;
		}
		return xmlFormat.toString();
	}


	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for Ability
	 * parameters.
	 * 
	 * @param outputXmlAsString
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 */
	public static Map<String, CommandParam> fromAbilityXmlToParameters(final String outputXmlAsString)
			throws EvaluationFailedException {
		
		final Map<String, CommandParam> outputParams = new LinkedHashMap();
		
		try {
			MessageFactory factory = MessageFactory.newInstance();
		    SOAPMessage message = factory.createMessage(
		            new MimeHeaders(),
		            new ByteArrayInputStream(outputXmlAsString.getBytes(Charset.forName("UTF-8"))));
	
		    SOAPBody body = message.getSOAPBody();
		    NodeList returnList = body.getElementsByTagName("API_OUTPUT");

		    for (int k = 0; k < returnList.getLength(); k++) {
		        NodeList innerResultList = returnList.item(k).getChildNodes();
		        for (int l = 0; l < innerResultList.getLength(); l++) {
		        	final CommandParamOutput commandParamOutput = new CommandParamOutput();
					commandParamOutput.setValue(innerResultList.item(l).getTextContent().trim());
					commandParamOutput.setPrimitiveValue(Primitives.STRING);
					commandParamOutput.setName(innerResultList.item(l).getNodeName().trim().toLowerCase());
					commandParamOutput.setType(CommandParameterType.PRIMITIVE);
					outputParams.put(innerResultList.item(l).getNodeName().trim().toLowerCase(), commandParamOutput);
		        }
		    }
		} catch (SOAPException | IOException e) {
			LOGGER.error("Error Occured while parsing ability output xml", e);
		}
		return outputParams;
	}

	/**
	 * This method is used to check if the Ability command executed successfully or
	 * failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if the status could not be evaluated.
	 */
	public static CommandExecutionStatus checkForAbilityCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		
		boolean isSuccessfull = false;
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandParam faultCodeParam = fdpCommand.getOutputParam(FDPCommandConstants.ABILITY_FAULT_CODE_PATH);
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		if (faultCodeParam != null) {
			isFailure = CommandUtil.checkFaultCode(configCache, faultCodeParam.getValue(), fdpCommand.getSystem());
			commandExecutionStatus.setCode((Integer) faultCodeParam.getValue());
			commandExecutionStatus.setDescription(fdpCommand.getOutputParam(
					FDPCommandConstants.ABILITY_FAULT_CODE_DESC_PATH).toString());
			((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
		} else {
			final CommandParam requestStatusParam = fdpCommand
					.getOutputParam(FDPCommandConstants.ABILITY_REQUEST_STATUS);
			
			if (requestStatusParam != null) {
				isSuccessfull = Integer.parseInt(requestStatusParam.getValue().toString()) == 0 ? true : false;
			}
			CommandParam responseCodeParam = fdpCommand
					.getOutputParam(FDPCommandConstants.ABILITY_RESPONSE_ERROR_CODE);
			
			if(isSuccessfull)
				responseCodeParam = requestStatusParam;
			
			if (responseCodeParam != null) {
				isFailure = CommandUtil.checkResponseCode(configCache, responseCodeParam.getValue(),
						fdpCommand.getCommandDisplayName());
				commandExecutionStatus.setCode(Integer.parseInt(responseCodeParam.getValue().toString()));
				commandExecutionStatus.setErrorType(ErrorTypes.RESPONSE_CODE.name());
				if (isFailure != null) {
					commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
					if(isFailure.getResultCodeType()==1)
						((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
				}
			}
		}
		if(isFailure==null)
			((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		return commandExecutionStatus;

	}
	
	private static String dateFormatFunction(String inputdate){
		String formatedDate = "";
		DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssZ");
        SimpleDateFormat dfNew = new SimpleDateFormat("YYYYMMdd");
		Date startDate;
		try {
			startDate = df.parse(inputdate);
			formatedDate = dfNew.format(startDate);
		} catch (ParseException e) {
		    LOGGER.error("Error Occured while parsing date for ablity parameter", e);
		}
		return formatedDate;
	}
	
	public static void writeCSVFileFromResponseXML(String xml){
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
		            	if(innerList.item(j).getAttributes().getNamedItem(ACTIVATION_DATE) !=null ){
		            		activationDate = innerList.item(j).getAttributes().getNamedItem(ACTIVATION_DATE).getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem(EXPIRY_DATE) !=null ){
		            		expiryDate = innerList.item(j).getAttributes().getNamedItem(EXPIRY_DATE).getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem("OFFER_CHARGE") !=null ){
		            		offerCharge = innerList.item(j).getAttributes().getNamedItem("OFFER_CHARGE").getNodeValue();
		            	}
		            	if(innerList.item(j).getAttributes().getNamedItem("ORDER_STATUS") !=null ){
		            		orderStatus = innerList.item(j).getAttributes().getNamedItem("ORDER_STATUS").getNodeValue();
		            	}
		            	
		            }
		        }
		        FileWriter fileWriter = null;
		        try {

		        	Date date = new Date() ;
		        	String fileFormat = PropertyUtils.getProperty("FILE_FORMAT").trim();
		        	String filePath = PropertyUtils.getProperty("FILE_PATH").trim();
		        	SimpleDateFormat dateFormat = null;
		        	if("DAILY".equalsIgnoreCase(fileFormat)){
		        		 dateFormat = new SimpleDateFormat("ddMMyyyy") ;
		        	}else{
		        		dateFormat = new SimpleDateFormat("ddMMyyyy_HH") ;
		        	}
			        
			        String fileName = filePath+File.separator+"ability_"+dateFormat.format(date) + ".csv";
			       
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

		        	LOGGER.debug("AbilitySyncUp: CSV file was created successfully Path: ---->>"+fileName);
		        } catch (Exception e) {
		        	LOGGER.error("AbilitySyncUp: Error in CsvFileWriter !!!"+ e);
		        } finally {
		        	try {
		        		fileWriter.flush();
		        		fileWriter.close();
		        	} catch (IOException e) {
		        		LOGGER.error("AbilitySyncUp: Error while flushing/closing fileWriter !!!");
		        	}
		        }

		    }	
		} catch (SOAPException | IOException e) {
			LOGGER.error("Error Occured while writing ability input xml", e);
		}
	}
	
	private static StringBuilder appendDateParamToXML(StringBuilder xmlFormat, CommandParam commandParam) {
	    xmlFormat.append(" ").append(commandParam.getName()).append("=")
        .append("\""+dateFormatFunction(commandParam.getValue().toString())+"\"");
        return xmlFormat;
    }
}
