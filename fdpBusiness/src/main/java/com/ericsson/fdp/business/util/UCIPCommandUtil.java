package com.ericsson.fdp.business.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;

/**
 * This is a utility class that deals with UCIP commands.
 * 
 * @author Ericsson
 * 
 */
public class UCIPCommandUtil {

	/**
	 * Instantiates a new uCIP command util.
	 */
	private UCIPCommandUtil() {

	}

	/**
	 * This method is used to create string representation of xml for an UCIP
	 * command. The supported version is 5.0 for UCIP commands. The
	 * implementation will be changed to support a different version.
	 * 
	 * @param command
	 *            The command for which the xml representation is to be created.
	 * @return The string representation of the command.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toUCIPXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder(1024);
		xmlFormat.append("<?xml version=\"1.0\"?>").append("\n").append("<methodCall>").append("\n")
				.append("<methodName>").append(command.getCommandName()).append("</methodName>").append("\n")
				.append("<params>").append("\n").append("<param>").append("\n").append("<value>").append("\n")
				.append("<struct>").append("\n")
				.append(UCIPCommandUtil.toUCIPParametersXmlFormat(command.getInputParam())).append("</struct>")
				.append("\n").append("</value>").append("\n").append("</param>").append("\n").append("</params>")
				.append("\n").append("</methodCall>");
		return xmlFormat.toString();
	}

	/**
	 * This method creates the string representation of xml for UCIP parameters
	 * used in the command.
	 * 
	 * @param inputParam
	 *            The list of parameters for which the string representation is
	 *            to be created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toUCIPParametersXmlFormat(final List<CommandParam> inputParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder(1024);
		for (final CommandParam commandParam : inputParam) {
			xmlFormat.append("<member>").append("\n").append("<name>").append(commandParam.getName()).append("</name>")
					.append("\n").append("<value>").append("\n").append(CommandParamUtil.toXmlForParam(commandParam))
					.append("</value>").append("\n").append("</member>").append("\n");
		}
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for UCIP
	 * parameters.
	 * 
	 * @param outputXmlAsString
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 */
	public static Map<String, CommandParam> fromUCIPXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		//System.out.println("inside UCIPXMLToParameters :: "+outputParam);
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		final List<String> pathList = new ArrayList<String>();
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			//System.out.println("JSON From XML :: "+xmlJSONObj);
			final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("methodResponse");
			JSONObject value = null;
			if (CommandUtil.checkIfFaultCode(methodResponse)) {
				final JSONObject faultCode = (JSONObject) methodResponse.get("fault");
				value = (JSONObject) faultCode.get("value");
			} else {
				final JSONObject params = (JSONObject) methodResponse.get("params");
				final JSONObject param = (JSONObject) params.get("param");
				value = (JSONObject) param.get("value");
			}
			CommandParamUtil.fromXmlToParameters(outputParams, pathList, value);
		} catch (final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	}

	/**
	 * This method is used to check if the UCIP command executed successfully or
	 * failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if the status could not be evaluated.
	 */
	public static CommandExecutionStatus checkForUCIPCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandParam faultCodeParam = fdpCommand.getOutputParam(FDPCommandConstants.UCIP_FAULT_CODE_PATH);
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		if (faultCodeParam != null) {
			isFailure = CommandUtil.checkFaultCode(configCache, faultCodeParam.getValue(), fdpCommand.getSystem());
			commandExecutionStatus.setCode((Integer) faultCodeParam.getValue());
			/*commandExecutionStatus.setDescription(fdpCommand.getOutputParam(
					FDPCommandConstants.UCIP_FAULT_CODE_DESC_PATH).toString());*/
			
			// Changes done to parse correct fault description
			if (fdpCommand.getOutputParam(FDPCommandConstants.UCIP_FAULT_CODE_DESC_PATH) != null) {
				commandExecutionStatus.setDescription(fdpCommand.getOutputParam(
						FDPCommandConstants.UCIP_FAULT_CODE_DESC_PATH).getValue().toString());
			}
			//setting write flag to true
			((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
			
		} else {
			final CommandParam responseCodeParam = fdpCommand
					.getOutputParam(FDPCommandConstants.UCIP_RESPONSE_CODE_PATH);
			if (responseCodeParam != null) {
				isFailure = CommandUtil.checkResponseCode(configCache, responseCodeParam.getValue(),
						fdpCommand.getCommandDisplayName());
				commandExecutionStatus.setCode((Integer) responseCodeParam.getValue());
				commandExecutionStatus.setErrorType(ErrorTypes.RESPONSE_CODE.name());
				if (isFailure != null) {
					commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
					//setting write flag to true
					if(isFailure.getResultCodeType()==1)
						((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
				}
			}
		}
		//setting write flag to true
		if(isFailure==null)
			((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		return commandExecutionStatus;

	}

}
