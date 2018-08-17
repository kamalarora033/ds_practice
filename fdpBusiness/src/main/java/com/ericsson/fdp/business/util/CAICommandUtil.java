
package com.ericsson.fdp.business.util;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * The CAI command utility.
 * 
 * @author Ericsson
 */
public class CAICommandUtil {


	/**
	 * The command identifier seperator.
	 */
	private static final String COMMAND_IDENTIFIER_SEPERATOR = ":";
	/**
	 * The parameter identifier seperator.
	 */
	private static final String PARAMETER_IDENTIFIER_SEPERATOR = ":";
	/**
	 * The parameter key value seperator.
	 */
	private static final String KEY_VALUE_SEPERATOR = ",";
	/**
	 * The parameters seperator.
	 */
	private static final String PARAMETER_SEPERATOR = ":";
	/**
	 * The end seperator.
	 */
	private static final String END_CHARACTER = ";";
	/**
	 * The result code.
	 */
	private static final String RESULT_CODE = "RESULT_CODE";
	
	
	/**
	 * The result code.
	 */
	private static final String RESULT_CODE_VALUE = "RESULT_CODE_VALUE";
	private static final String PARAMETER_LOG_SEPERATOR = "#";
	private static final String KEY_VALUE_SEPERATOR_LOG = "=";
	private static final String RESPONSE_CODE = "resp";

	/**
	 * Instantiates a new cAI command util.
	 */
	private CAICommandUtil() {

	}
	/**
	 * This method is used to check for the CAI command status.
	 * 
	 * @param fdpCommand
	 *            the command for which status is to be checked.
	 * @return the command execution status.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static CommandExecutionStatus checkForCIACommandStatus(
			final FDPCommand fdpCommand) throws ExecutionFailedException {
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		FDPResultCodesDTO isFailure = null;
		final CommandParam responseCodeParam = fdpCommand.getOutputParam(RESULT_CODE);
		if (responseCodeParam != null) {
			isFailure = checkResponseCode(responseCodeParam.getValue(), fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode((Integer) responseCodeParam.getValue());
			if (isFailure != null) {
				commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
				if(isFailure.getResultCodeType()==1)
					((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
			}
		}
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		if(isFailure==null)
			((AbstractCommand)fdpCommand).setWriteToFailureFile(true);

		return commandExecutionStatus;
	}

	/**
	 * This method is used to check for result code for failure.
	 * 
	 * @param value
	 *            the value found.
	 * @param commandDisplayName
	 *            the command display name.
	 * @return the result found.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static FDPResultCodesDTO checkResponseCode(final Object value,
			final String commandDisplayName) throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil
				.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final Object responseCodeMapping = configCache
				.getValue(new FDPAppBag(AppCacheSubStore.COMMAND_RESULT_CODES_MAPPING,
						commandDisplayName + FDPConstant.PARAMETER_SEPARATOR + value));
		if (responseCodeMapping instanceof FDPResultCodesDTO) {
			isFailure = (FDPResultCodesDTO) responseCodeMapping;
		}
		if (isFailure == null) {
			isFailure = checkForCAIValue(value);
		}
		return isFailure;
	}

	/**
	 * This method is used to check for CAI value.
	 * 
	 * @param value
	 *            the value.
	 * @return the result code.
	 */
	private static FDPResultCodesDTO checkForCAIValue(final Object value) {
		FDPResultCodesDTO fdpResultCodesDTO = null;
		try {
			final Integer valueInLong = Integer.parseInt(value.toString());
			fdpResultCodesDTO = getResultCode(valueInLong);
			if (valueInLong >= 1000 && valueInLong <= 1999) {
				fdpResultCodesDTO
						.setResultCodeDesc("communication problem between EMA->HLR");
			} else if (valueInLong >= 2000 && valueInLong <= 2999) {
				fdpResultCodesDTO.setResultCodeDesc("EMA internal errors");
			} else if (valueInLong >= 3000 && valueInLong <= 3999) {
				fdpResultCodesDTO
						.setResultCodeDesc("login problems, CAI command format erros");
			}
		} catch (final NumberFormatException e) {
			// Do Nothing if value is not long.
		}
		return fdpResultCodesDTO;
	}

	/**
	 * This method is used to create a result code.
	 * 
	 * @param valueInLong
	 *            the value in long.
	 * @return the result code.
	 */
	private static FDPResultCodesDTO getResultCode(final Integer valueInLong) {
		FDPResultCodesDTO fdpResultCodesDTO;
		fdpResultCodesDTO = new FDPResultCodesDTO();
		fdpResultCodesDTO.setIsRollback(Status.FAILURE);
		fdpResultCodesDTO.setExternalSystemInterface(ExternalSystem.EMA);
		fdpResultCodesDTO.setResultCodeValue(valueInLong);
		fdpResultCodesDTO.setResultCodeType(1);
		return fdpResultCodesDTO;
	}

	/**
	 * This method is used to create the command to CAI format.
	 * 
	 * @param command
	 *            the command to be executed.
	 * @return the string format for the command.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static String toCAIFormat(final FDPCommand command)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		for (final CommandParam commandParam : command.getInputParam()) {
			xmlFormat.append(getParamString(commandParam));
		}
		String xmlString = xmlFormat.toString();
		if (xmlString.length() > 0) {
			// Remove the last identifier.
			xmlString = xmlString.substring(0, xmlString.length() - 1);
		}
		xmlString += END_CHARACTER;
		return xmlString;
	}

	/**
	 * This method is used to create the parameter string.
	 * 
	 * @param commandParam
	 *            the command param.
	 * @return the parameter as string.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private static String getParamString(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PARAM_IDENTIFIER:
			xmlFormat.append(commandParam.getValue())
					.append(PARAMETER_IDENTIFIER_SEPERATOR)
					.append(getParameterSeprator(commandParam));
			break;
		case COMMAND_IDENTIFIER:
			xmlFormat.append(commandParam.getValue())
			.append(getParameterSeprator(commandParam));
			break;
		case PRIMITIVE:
			xmlFormat.append(getParamName(commandParam.getName()))
					.append(KEY_VALUE_SEPERATOR)
					.append(commandParam.getValue().toString())
					.append(getParameterSeprator(commandParam));
			break;
		default:
			throw new ExecutionFailedException(
					"The command parameter type is not recognized. It is of type "
							+ commandParam.getType());
		}
		return xmlFormat.toString();
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	private static  String getParamName(String name) {
		if(name.endsWith("_COL")||name.endsWith("_COMM"))
		{
		return	name.substring(0, name.lastIndexOf('_'));
		}
		else 
		{
			return name;
		}
		
		
	}

	/**
	 * new parameter sepration method added to find the end param it is detected based on parameter name is
	 post fixed with _com or _col all CAI command params have to follow this convention  
	 * @param commandParam
	 * @return
	 */
	private static Object getParameterSeprator(CommandParam commandParam) {
		if (commandParam.getName().endsWith("_COMM")) {
			return KEY_VALUE_SEPERATOR;
		} else if (commandParam.getName().endsWith("_COL")) {
			return COMMAND_IDENTIFIER_SEPERATOR;
		}
		
		return COMMAND_IDENTIFIER_SEPERATOR;
	}

	/**
	 * This method is used to convert output to parameters.
	 * 
	 * @param outputXmlAsString
	 *            the output.
	 * @return the map of output.
	 */
	public static Map<String, CommandParam> fromCAIToParameters(
			String outputXmlAsString) {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<>();
		CommandParamOutput fdpCommandParamOutput = new CommandParamOutput();
		String resultcode;
		String outputXmlAsStringCommandParams =outputXmlAsString;
		
		fdpCommandParamOutput.setPrimitiveValue(Primitives.INTEGER);
		fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
		
		resultcode = getResponseString(outputXmlAsString);
		String[] resultarr=resultcode.split(":");
		fdpCommandParamOutput
				.setValue(Integer.valueOf(resultarr[resultarr.length-1]));
		outputParams.put(RESULT_CODE.toLowerCase(), fdpCommandParamOutput);
		outputParams.put(RESPONSE_CODE, fdpCommandParamOutput);
		
		fdpCommandParamOutput = new CommandParamOutput();
		fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
		fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
		
		outputXmlAsStringCommandParams = outputXmlAsStringCommandParams.substring(
				outputXmlAsStringCommandParams.indexOf(COMMAND_IDENTIFIER_SEPERATOR) + 1,
				outputXmlAsStringCommandParams.length());
		fdpCommandParamOutput.setValue(outputXmlAsStringCommandParams);
		outputParams
				.put(RESULT_CODE_VALUE.toLowerCase(), fdpCommandParamOutput);
		setCommandParams(outputXmlAsStringCommandParams,outputParams);
		return outputParams;
	}

	private static void setCommandParams(String outputXmlAsString,
			Map<String, CommandParam> outputParams) {
		
		String[] splittedOutput=outputXmlAsString.split(FDPConstant.COLON);
		String[] splittedoutputtem;
		for(String str:splittedOutput)
		{
			str=str.replace(";", "").replace("\"", "");
			splittedoutputtem=str.split(FDPConstant.COMMA);
			if(splittedoutputtem.length>FDPConstant.ONE)
			{
				CommandParamOutput commandParam=new CommandParamOutput();
				commandParam.setPrimitiveValue(Primitives.STRING);
				commandParam.setType(CommandParameterType.PRIMITIVE);
				commandParam.setValue(getCommandParamValue(splittedoutputtem));	
				outputParams.put(splittedoutputtem[0].toLowerCase(), commandParam);
			}
		}
	
	}

	private static String getResponseString(String outputXmlAsString) {

		String[] arr = outputXmlAsString.split(COMMAND_IDENTIFIER_SEPERATOR);
		String output = null;
		for (int i = 0; i < arr.length; i++) {
			if ("RESP".equalsIgnoreCase(arr[i].trim())) {
				output = arr[i].trim() + COMMAND_IDENTIFIER_SEPERATOR
						+ arr[i + 1].trim();
				if(output.contains(";"))
				{
					output=output.replace(";", "");
				}
				break;
			}
		}
		return output;
	}

	/**
	 * This method is used to get the logs for CAI format.
	 * 
	 * @param command
	 *            the command to be logged.
	 * @return the string to log.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static String toCAIFormatForLog(final FDPCommand command)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		for (final CommandParam commandParam : command.getInputParam()) {
			xmlFormat.append(getParamStringForLog(commandParam));
		}
		String xmlString = xmlFormat.toString();
		if (xmlString.length() > 0) {
			// Remove the last identifier.
			xmlString = xmlString.substring(0, xmlString.length() - 1);
		}
		xmlString += END_CHARACTER;
		return xmlString;
	}

	/**
	 * This method is used to get the param string for log.
	 * 
	 * @param commandParam
	 *            the command param to use.
	 * @return the string.
	 * @throws ExecutionFailedException
	 */
	private static String getParamStringForLog(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PARAM_IDENTIFIER:
			xmlFormat.append(commandParam.getValue())
					.append(PARAMETER_IDENTIFIER_SEPERATOR)
					.append(PARAMETER_SEPERATOR);
			break;
		case COMMAND_IDENTIFIER:
			xmlFormat.append(commandParam.getValue()).append(
					COMMAND_IDENTIFIER_SEPERATOR);
			break;
		case PRIMITIVE:
			xmlFormat.append(commandParam.getName())
					.append(KEY_VALUE_SEPERATOR_LOG)
					.append(commandParam.getValue().toString())
					.append(PARAMETER_LOG_SEPERATOR);
			break;
		default:
			throw new ExecutionFailedException(
					"The command parameter type is not recognized. It is of type "
							+ commandParam.getType());
		}
		return xmlFormat.toString();
	}
	
	private static List<String> getCommandParamValue(String[] commandParam) {
		int len = commandParam.length;
		List<String> list = new ArrayList<>();

		for (int i = 1; i < len; i++) {
			list.add(commandParam[i]);
		}
		return list; 
	}

}
