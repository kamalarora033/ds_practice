package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

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

public class EVDSHttpCommandUtil {

	
	private EVDSHttpCommandUtil() {
		
	};
	
	
	/**
	 * Check for fdpOffLine command status.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @return the command execution status
	 * @throws ExecutionFailedException 
	 */
	public static CommandExecutionStatus checkForEVDSCommandStatus(final FDPCommand fdpCommand) throws ExecutionFailedException {
		
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		//final CommandParam faultCodeParam = fdpCommand.getOutputParam(FDPCommandConstants.UCIP_FAULT_CODE_PATH);
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		final CommandParam responseCodeParam = fdpCommand.getOutputParams().get("resultCode");
			if (responseCodeParam != null) {
				isFailure = CommandUtil.checkResponseCode(configCache, responseCodeParam.getValue(),
						fdpCommand.getCommandDisplayName());
				commandExecutionStatus.setCode((Integer) responseCodeParam.getValue());
				commandExecutionStatus.setErrorType(ErrorTypes.RESPONSE_CODE.name());
				if (isFailure != null) {
					commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
					if(isFailure.getResultCodeType()==1)
						((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
				}
			}
			if(isFailure==null)
				((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		return commandExecutionStatus;

	
	}
	
	
	
	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for MobileMoney
	 * parameters.
	 * 
	 * @param outputParam
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 */

	public static Map<String, CommandParam> fromEVDSXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		

		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		final List<String> pathList = new ArrayList<String>();
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			listJSONObject("", xmlJSONObj,outputParams);
		} catch (final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	
		
		
	}
	
	private static void listJSONObject(String parent, JSONObject json, Map<String, CommandParam> outputParams) throws JSONException {
	    Iterator it = json.keys();
	    while (it.hasNext()) {
	      String key = (String)it.next();
	      Object child = json.get(key);
	      String childKey = parent.isEmpty() ? key : parent + "." + key;
	      listObject(childKey, child,outputParams);
	    }
	  }

	  private static void listJSONArray(String parent, JSONArray json,Map<String, CommandParam> outputParams) throws JSONException {
	    for (int i = 0; i < json.length(); i++) {
	      Object data = json.get(i);
	      listObject(parent, data,outputParams);
	    }
	  }

	  private static void listPrimitive(String parent, Object obj, Map<String, CommandParam> outputParams) {
	    System.out.println(parent + ":"  + obj);
	    
	    
	    if(!parent.contains("xmlns") && !parent.contains("xsi"))
	    {
	    	CommandParamOutput fdpCommandParamOutput = new CommandParamOutput();
	    	String split[]=parent.split("\\.");
	    	
	    	fdpCommandParamOutput = new CommandParamOutput();
			fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
			fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
			fdpCommandParamOutput.setValue(obj);
			outputParams.put(split[split.length-1], fdpCommandParamOutput);
			
	    }
	  }
	  
	  private static void listObject(String parent, Object data, Map<String, CommandParam> outputParams) throws JSONException {
		    if (data instanceof JSONObject) {
		      listJSONObject(parent, (JSONObject)data, outputParams);
		    } else if (data instanceof JSONArray) {
		      listJSONArray(parent, (JSONArray) data,outputParams);
		    } else {
		      listPrimitive(parent, data,outputParams);
		    }    
		  }
	  
	
	/**
	 * This method is used to create string representation of xml for an Mobile Money Command
	 * command.  
	 * @param command
	 *            The command for which the xml representation is to be created.
	 * @return The string representation of the command.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toEVDSXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		
		xmlFormat.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n")
		         .append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"")
		         .append(" xmlns:ext=\"http://external.interfaces.ers.seamless.com/\">").append("\n")
		         .append("<soapenv:Header/>").append("\n")
		         .append("<soapenv:Body>").append("\n")
		         .append("<ext:"+ command.getCommandName()+">").append("\n")
				 .append(toEVDSParametersXmlFormat(command.getInputParam()))
				 .append("</ext:"+ command.getCommandName()+">").append("\n")
				 .append("</soapenv:Body>").append("\n")
				 .append("</soapenv:Envelope>");
		return xmlFormat.toString();
	}
	
	
	/** This method is used to Close the mobilemoney command
		*/
	private static Object populateFoot(final FDPCommand command) {
		// TODO Auto-generated method stub
		return "</p:"+command.getCommandName()+">";
	}


	public static Object toEVDSParametersXmlFormat(List<CommandParam> inputParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		List<CommandParam> inputParamtmp=new ArrayList<CommandParam>();
		for (Iterator iterator = inputParam.iterator(); iterator.hasNext();) {
			CommandParam commandParam1 = (CommandParam) iterator.next();
			if(!commandParam1.getName().contains("xmlns") & !commandParam1.getName().contains("xsi"))
			{
				inputParamtmp.add(commandParam1);
			}
		}
		for (final CommandParam commandParam : inputParamtmp) {

			xmlFormat.append(toXmlForParam(commandParam));
		}
		return xmlFormat.toString();
	}
	
	public static String toXmlForParam(final CommandParam commandParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case ARRAY:
			xmlFormat.append(toXmlFromArrayParam(commandParam));
			break;
		case PARAM_IDENTIFIER:
		case COMMAND_IDENTIFIER:
		case PRIMITIVE:
			xmlFormat.append(toXmlfromPrimitiveParam(commandParam));
			break;
		case STRUCT:
			xmlFormat.append(toXmlFromStructParam(commandParam));
			break;
		default:
			throw new ExecutionFailedException("The command parameter type is not recognized. It is of type "
					+ commandParam.getType());
		}
		return xmlFormat.toString();
	}
	

	/**
	 * This method is used to create a string representation of the xml for a
	 * parameter of type struct.
	 * 
	 * @param commandParam
	 *            The parameter for which the string representation is to be
	 *            created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the string representation could not be created.
	 */
	private static String toXmlFromStructParam(final CommandParam commandParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("<"+commandParam.getName()+">").append("\n");
		for (final CommandParam fdpCommandParam : commandParam.getChilderen()) {
			xmlFormat			
			.append(toXmlForParam(fdpCommandParam));
			
		}
		xmlFormat.append("</"+commandParam.getName()+">").append("\n");
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a string representation of the xml for
	 * primitive type.
	 * 
	 * @param commandParam
	 *            The parameter for which the string representation is to be
	 *            created.
	 * @return The string representation.
	 */
	private static String toXmlfromPrimitiveParam(final CommandParam commandParam) {
		final StringBuilder xmlFormat = new StringBuilder();
		final String primitiveType = commandParam.getName();
		xmlFormat.append("<").append(primitiveType).append(">").append(commandParam.getValue().toString()).append("</")
		.append(primitiveType).append(">").append("\n");
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a string representation of the xml for
	 * parameter of type array.
	 * 
	 * @param commandParam
	 *            The parameter for which the string representation is to be
	 *            created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the string representation could not be created.
	 */
	private static String toXmlFromArrayParam(final CommandParam commandParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("<array>").append("\n").append("<data>").append("\n");
		for (final CommandParam fdpCommandParam : commandParam.getChilderen()) {
			xmlFormat.append("<value>").append("\n").append(toXmlForParam(fdpCommandParam)).append("</value>")
			.append("\n");
		}
		xmlFormat.append("</data>").append("\n").append("</array>").append("\n");
		return xmlFormat.toString();
	}

	
}
