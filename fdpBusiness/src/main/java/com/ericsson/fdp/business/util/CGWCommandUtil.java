package com.ericsson.fdp.business.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
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

/**
 * This is a utility class that deals with CGW commands.
 * 
 * @author Ericsson
 * 
 */
public class CGWCommandUtil {

	/**
	 * Instantiates a new cGW command util.
	 */
	private CGWCommandUtil() {

	}

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for CGW
	 * parameters.
	 * 
	 * @param outputXmlAsString
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 */
	public static Map<String, CommandParam> fromCGWXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("DebitResponse");
			final Integer resultCode = Integer.parseInt(methodResponse.get("returnCode").toString());
			final CommandParamOutput commandParamOutput = new CommandParamOutput();
			commandParamOutput.setValue(resultCode);
			commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
			outputParams.put("returnCode".toLowerCase(), commandParamOutput);
		} catch (final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	}

	/**
	 * This method is used to create string representation of xml for an CGW
	 * command. The supported version is 5.0 for ACIP commands. The
	 * implementation will be changed to support a different version.
	 * 
	 * @param command
	 *            The command for which the xml representation is to be created.
	 * @return The string representation of the command.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toCGWXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder(512);
		xmlFormat.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>").append("\n").append("<")
				.append(command.getCommandName()).append(">")
				.append(CGWCommandUtil.toCGWParametersXmlFormat(command.getInputParam())).append("</")
				.append(command.getCommandName()).append(">");
		return xmlFormat.toString();
	}

	/**
	 * This method creates the string representation of xml for CGW parameters
	 * used in the command.
	 * 
	 * @param inputParam
	 *            The list of parameters for which the string representation is
	 *            to be created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toCGWParametersXmlFormat(final List<CommandParam> inputParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder(256);
		for (final CommandParam commandParam : inputParam) {
			if(commandParam.getName() != null)
			xmlFormat.append(toXmlfromPrimitiveParam(commandParam));
		}
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
		xmlFormat.append("<").append(commandParam.getName()).append(">").append(commandParam.getValue().toString())
				.append("</").append(commandParam.getName()).append(">").append("\n");
		return xmlFormat.toString();
	}

	/**
	 * This method is used to check if the CGW command executed successfully or
	 * failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if the status could not be evaluated.
	 */
	public static CommandExecutionStatus checkForCGWCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.RESPONSE_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandParam responseCodeParam = fdpCommand.getOutputParam(FDPCommandConstants.CGW_RESPONSE_CODE_PATH);
		if (responseCodeParam != null) {
			isFailure = CommandUtil.checkResponseCode(configCache, responseCodeParam.getValue(),
					fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode((Integer) responseCodeParam.getValue());
		}
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		if (isFailure != null) {
			commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
		}
		return commandExecutionStatus;
	}

}
