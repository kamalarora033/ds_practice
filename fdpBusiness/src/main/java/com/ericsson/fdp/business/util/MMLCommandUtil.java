package com.ericsson.fdp.business.util;

import java.util.LinkedHashMap;
import java.util.Map;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.MMLCommandConstants;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
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
 * The utility for MML commands.
 * 
 * @author Ericsson
 * 
 */
public class MMLCommandUtil {

	/**
	 * Instantiates a new mML command util.
	 */
	private MMLCommandUtil() {

	}

	/**
	 * The command identifier seperator.
	 */
	private static final String COMMAND_IDENTIFIER_SEPERATOR = ":";
	/**
	 * The parameter key value seperator.
	 */
	private static final String KEY_VALUE_SEPERATOR = "=";
	/**
	 * The parameters seperator.
	 */
	private static final String PARAMETER_SEPERATOR = ",";
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
	/**
	 * The not accepted case.
	 */
	private static final String NOT_ACCEPTED = "NOT ACCEPTED";
	private static final String PARAMETER_SEPERATOR_FOR_LOG = "#";

	/**
	 * This method is used to check for the MML command status.
	 * 
	 * @param fdpCommand
	 *            the command for which status is to be checked.
	 * @return the command execution status.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static CommandExecutionStatus checkForMMLCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
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
			}
		}
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
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
	public static FDPResultCodesDTO checkResponseCode(final Object value, final String commandDisplayName)
			throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final Object responseCodeMapping = configCache.getValue(new FDPAppBag(
				AppCacheSubStore.COMMAND_RESULT_CODES_MAPPING,
				(commandDisplayName + FDPConstant.PARAMETER_SEPARATOR + value)));
		if (responseCodeMapping instanceof FDPResultCodesDTO) {
			isFailure = (FDPResultCodesDTO) responseCodeMapping;
		}
		return isFailure;
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
	private static String getParamString(final CommandParam commandParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PARAM_IDENTIFIER:
			xmlFormat.append(commandParam.getValue()).append(PARAMETER_SEPERATOR);
			break;
		case COMMAND_IDENTIFIER:
			xmlFormat.append(commandParam.getValue()).append(COMMAND_IDENTIFIER_SEPERATOR);
			break;
		case PRIMITIVE:
			xmlFormat.append(commandParam.getName()).append(KEY_VALUE_SEPERATOR)
					.append(commandParam.getValue().toString()).append(PARAMETER_SEPERATOR);
			break;
		default:
			throw new ExecutionFailedException("The command parameter type is not recognized. It is of type "
					+ commandParam.getType());
		}
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create the command to MML format.
	 * 
	 * @param command
	 *            the command to be executed.
	 * @return the string format for the command.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static String toMMLFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		String xmlString = null;
		for (final CommandParam commandParam : command.getInputParam()) {
			xmlFormat.append(getParamString(commandParam));
		}
		xmlString = xmlFormat.toString();
		if (xmlString.length() > 0) {
			// Remove the last identifier.
			xmlString = xmlString.substring(0, xmlString.length() - 1);
		}
		xmlString += (END_CHARACTER);
		return xmlString;
	}

	public static void main(final String[] args) {
		 //System.out.println(fromMMLToParameters("<HGSDP:MSISDN=918129149806,SUDA;\nHGSDP:MSISDN=918129149806,SUDA;\nHLR SUBSCRIBER DATA\n\nSUBSCRIBER IDENTITY\nMSISDN           IMSI             STATE          AUTHD\n918129149806     404950110113913  CONNECTED      AVAILABLE\n\nNAM  IMEISV\n0    3583480526596734\n\nPERMANENT SUBSCRIBER DATA\nSUD\nCAT-10       DBSG-1       TSMO-0       STYPE-13\nTS11-1       TS21-1       TS22-1       BS3G-1\nRSA-63       CSP-24       REDMCH-1     ARD-1\n\nOFA-1        PWD-1234     OICK-45      CFU-1\nCFB-1        CFNRY-1      CFNRC-1      BAOC-1BAIC-1       CAW-1        SOCFB-0      SOCFRY-0SOCFRC-0     SOCFU-0      SOCB-0       SOCLIP-0HOLD-1       MPTY-1       CLIP-1SCHAR-4-0AMSISDN            BS       BCNONEEND<exit;"));
		//System.out.println("NOT ACCEPTEDFAULT CODE 14".hashCode());
		System.out.println(fromMMLToParameters("<HGSDP:MSISDN=919810460224,ALL;\nHLR SUBSCRIBER DATA\n\nSUBSCRIBER IDENTITY\nMSISDN           IMSI             STATE          AUTHD\n919810460224     404100128131420  CONNECTED      AVAILABLE\n\nNAM  IMEISV\n0    3578920531862434\n\nPERMANENT SUBSCRIBER DATA\nSUD\nCAT-10       DBSG-1       TSMO-0       STYPE-9\nBS26-1       TS11-1       TS21-1       TS22-1\nRSA-63       CSP-5        REDMCH-1     ARD-1\nOFA-1        PWD-1234     OICK-5       CFU-1\nCFB-1        CFNRY-1      CFNRC-1      BAOC-1\nBAIC-1       CAW-1        SOCFB-0      SOCFRY-0\nSOCFRC-0     SOCFU-0      SOCB-0       SOCLIP-0\nHOLD-1       MPTY-1       CLIP-1\nSCHAR-4-0\n\nAMSISDN            BS       BC\nNONE\n\nSUPPLEMENTARY SERVICE DATA\nBSG\nTS10\nSS       STATUS        FNUM                 TIME\n                       SADD\nBAIC     NOT ACTIVE\nBAOC     NOT ACTIVE\nCAW      NOT ACTIVE\nCFU      NOT ACTIVE\nCFB      NOT ACTIVE\nCFNRY    NOT ACTIVE\nCFNRC    NOT ACTIVE\n\nBSG\nTS20\nSS       STATUS        FNUM                 TIME\n                       SADD\nBAIC     NOT ACTIVE\nBAOC     NOT ACTIVE\n\nBSG\nBS20\nSS       STATUS        FNUM                 TIME\n                       SADD\nBAIC     NOT ACTIVE\nBAOC     NOT ACTIVE\nCAW      NOT ACTIVE\nCFU      NOT ACTIVE\nCFB      NOT ACTIVE\nCFNRY    NOT ACTIVE\nCFNRC    NOT ACTIVE\n\nLOCATION DATA\nVLR ADDRESS       MSRN            MSC NUMBER          LMSID\n4-919810051968                    919810051968\n\nSGSN NUMBER\n4-919810151263\n\nPACKET DATA PROTOCOL CONTEXT DATA\nAPNID   PDPADD                EQOSID  VPAA  PDPCH    PDPTY  PDPID\n   26                            7    NO             IPV4    1\n    7                            7    NO             IPV4   10\nEND\n<"));
	}

	/**
	 * This method is used to convert output to parameters.
	 * 
	 * @param outputXmlAsString
	 *            the output.
	 * @return the map of output.
	 */
	public static Map<String, CommandParam> fromMMLToParameters(final String outputXmlAsString) {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		final String[] outputArray = outputXmlAsString.split(MMLCommandConstants.MML_DELIMITER);
		StringBuffer resultStringBuffer = new StringBuffer();
		boolean notAcceptedCase = false, resultFound = false;
		int skip = 1;
		String inputString = FDPConstant.EMPTY_STRING;
		for (final String outputString : outputArray) {
			if (skip > 0) {
				skip = skip >> 1;
				inputString = outputString.trim();
				continue;
			}
			if (outputString.isEmpty() || outputString.equalsIgnoreCase(MMLCommandConstants.MML_DELIMITER) || inputString.contains(outputString.trim())) {
				inputString = FDPConstant.EMPTY_STRING;
				continue;
			}
			inputString = FDPConstant.EMPTY_STRING;
			resultStringBuffer.append(outputString);
			notAcceptedCase = false;
			if (!resultFound && NOT_ACCEPTED.equalsIgnoreCase(outputString)) {
				notAcceptedCase = true;
			}
			if (!notAcceptedCase && !resultFound) {
				resultFound = true;
				CommandParamOutput fdpCommandParamOutput = new CommandParamOutput();
				fdpCommandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
				fdpCommandParamOutput.setValue(Integer.valueOf(resultStringBuffer.toString().hashCode()));
				outputParams.put(RESULT_CODE.toLowerCase(), fdpCommandParamOutput);
				fdpCommandParamOutput = new CommandParamOutput();
				fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
				fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
				fdpCommandParamOutput.setValue(resultStringBuffer.toString());
				outputParams.put(RESULT_CODE_VALUE.toLowerCase(), fdpCommandParamOutput);
				resultStringBuffer = new StringBuffer();
			}
		}
		CommandParamOutput fdpCommandParamOutput = new CommandParamOutput();
		fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
		fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
		fdpCommandParamOutput.setValue(resultStringBuffer.toString());
		outputParams.put(MMLCommandConstants.RESPONSE.toLowerCase(), fdpCommandParamOutput);
		fdpCommandParamOutput = new CommandParamOutput();
		fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
		fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);
		fdpCommandParamOutput.setValue(outputXmlAsString);
		outputParams.put(MMLCommandConstants.RESPONSE_VALUES.toLowerCase(), fdpCommandParamOutput);
		return outputParams;
	}

	/**
	 * This method is used for mml format for log.
	 * 
	 * @param command
	 *            the command to log.
	 * @return the string to log.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	public static String toMMLFormatForLog(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		String xmlString = null;
		for (final CommandParam commandParam : command.getInputParam()) {
			xmlFormat.append(getParamStringForLog(commandParam));
		}
		xmlString = xmlFormat.toString();
		if (xmlString.length() > 0) {
			// Remove the last identifier.
			xmlString = xmlString.substring(0, xmlString.length() - 1);
		}
		xmlString += (END_CHARACTER);
		return xmlString;
	}

	/**
	 * This method is used to get param string for log.
	 * 
	 * @param commandParam
	 *            the command param.
	 * @return the logging value.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private static String getParamStringForLog(final CommandParam commandParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PARAM_IDENTIFIER:
			xmlFormat.append(commandParam.getValue()).append(COMMAND_IDENTIFIER_SEPERATOR);
			break;
		case COMMAND_IDENTIFIER:
			xmlFormat.append(commandParam.getValue()).append(COMMAND_IDENTIFIER_SEPERATOR);
			break;
		case PRIMITIVE:
			xmlFormat.append(commandParam.getName()).append(KEY_VALUE_SEPERATOR)
					.append(commandParam.getValue().toString()).append(PARAMETER_SEPERATOR_FOR_LOG);
			break;
		default:
			throw new ExecutionFailedException("The command parameter type is not recognized. It is of type "
					+ commandParam.getType());
		}
		return xmlFormat.toString();
	}
}
