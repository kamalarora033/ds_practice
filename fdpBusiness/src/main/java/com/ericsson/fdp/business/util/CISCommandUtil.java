package com.ericsson.fdp.business.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.constants.FDPCommandConstants;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ExternalSystem;
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
 * This is a utility class that deals with CIS commands.
 * 
 * @author Ericsson
 * 
 */
public class CISCommandUtil {

	/**
	 * Instantiates a new cis command util.
	 */
	private CISCommandUtil() {

	}

	/**
	 * This method is used to create string representation of xml for an cis
	 * command. The supported version is 5.0 for cis commands. The
	 * implementation will be changed to support a different version.
	 * 
	 * @param command
	 *            The command for which the xml representation is to be created.
	 * @return The string representation of the command.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toCisXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("<?xml version=\"1.0\"?>").append("\n").append("<methodCall>").append("\n")
				.append("<methodName>").append(command.getCommandName()).append("</methodName>").append("\n")
				.append("<params>").append("\n").append("<param>").append("\n").append("<value>").append("\n")
				.append("<struct>").append("\n")
				.append(CISCommandUtil.toCisParametersXmlFormat(command.getInputParam())).append("</struct>")
				.append("\n").append("</value>").append("\n").append("</param>").append("\n").append("</params>")
				.append("\n").append("</methodCall>");
		return xmlFormat.toString();
	}

	/**
	 * This method creates the string representation of xml for cis parameters
	 * used in the command.
	 * 
	 * @param inputParam
	 *            The list of parameters for which the string representation is
	 *            to be created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toCisParametersXmlFormat(final List<CommandParam> inputParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		for (final CommandParam commandParam : inputParam) {
			xmlFormat.append("<member>").append("\n").append("<name>").append(commandParam.getName()).append("</name>")
					.append("\n").append("<value>").append("\n").append(CommandParamUtil.toXmlForParam(commandParam))
					.append("</value>").append("\n").append("</member>").append("\n");
		}
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for cis
	 * parameters.
	 * 
	 * @param outputXmlAsString
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 */
	public static Map<String, CommandParam> fromCisXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			outputParams.putAll(getCISRecordParams(xmlJSONObj, "methodCall", "record"));
		} catch (final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	}

	private static Map<String, CommandParam> getCISRecordParams(final JSONObject methodResponse, final String rootTag,
			final String subTag) throws JSONException {
		final Map<String, CommandParam> outputParam = new HashMap<String, CommandParam>();
		final Object objectForMember = methodResponse.get(rootTag);
		if (objectForMember instanceof JSONArray) {
			final JSONArray jsonArray = (JSONArray) objectForMember;
			for (int i = 0; i < jsonArray.length(); i++) {
				final JSONObject arrayObject = (JSONObject) jsonArray.get(i);
				String commonPath = rootTag + BusinessConstants.DOT + subTag + BusinessConstants.DOT + i
						+ BusinessConstants.DOT;
				setCommandParams(outputParam, arrayObject, commonPath);
			}
		} else if (objectForMember instanceof JSONObject) {
			final JSONObject arrayObject = (JSONObject) objectForMember;
			setCommandParams(outputParam, arrayObject, "");
		}
		return outputParam;
	}

	private static void setCommandParams(final Map<String, CommandParam> outputParam, final JSONObject arrayObject,
			String commonPath) throws JSONException {
		setCommandOutputParams(outputParam, arrayObject, "internationalMsisdn", commonPath, Primitives.BOOLEAN);
		setCommandOutputParams(outputParam, arrayObject, "paysrc", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "iname", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "provisioningAction", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "defaultOfferID", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "responseCode", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "ussdSuccess", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "ussdFailure", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "smsSuccess", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "Year", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "Hour", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "Date", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "Day", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "Month", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "BuyForOther", commonPath, Primitives.BOOLEAN);
		setCommandOutputParams(outputParam, arrayObject, "buyForOther", commonPath, Primitives.INTEGER);
		setCommandOutputParams(outputParam, arrayObject, "getSystemTime", commonPath, Primitives.DATETIME);
		setCommandOutputParams(outputParam, arrayObject, "getsystemdate", commonPath, Primitives.DATETIME);
		setCommandOutputParams(outputParam, arrayObject, "getCurrentSystemTime", commonPath, Primitives.DATETIME);
		setCommandOutputParams(outputParam, arrayObject, "WeekDay", commonPath, Primitives.STRING);
		

	}

	private static void setCommandOutputParams(final Map<String, CommandParam> outputParam,
			final JSONObject arrayObject, final String paramName, final String commonPath, final Primitives primitives)
			throws JSONException {
		if (arrayObject.has(paramName)) {
			final CommandParamOutput commandParamOutput = new CommandParamOutput();
			commandParamOutput.setValue(arrayObject.get(paramName).toString());
			commandParamOutput.setPrimitiveValue(primitives);
			outputParam.put((commonPath + paramName).toLowerCase(), commandParamOutput);
		}
	}

	/**
	 * This method is used to check if the cis command executed successfully or
	 * failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if the status could not be evaluated.
	 */
	public static CommandExecutionStatus checkForCisCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.SUCCESS, 200,
				FDPConstant.RESULT_SUCCESS, ErrorTypes.RESPONSE_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		FDPResultCodesDTO isFailure = null;
		final CommandParam responseCodeParam = fdpCommand.getOutputParam(FDPCommandConstants.CIS_RESPONSE_CODE_PATH);
		if (responseCodeParam != null) {
			isFailure = CommandUtil.checkResponseCode(configCache, responseCodeParam.getValue(),
					fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode(Integer.parseInt(responseCodeParam.getValue().toString()));
			commandExecutionStatus.setErrorType(ErrorTypes.RESPONSE_CODE.name());
			if (isFailure != null) {
				commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
				commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
			}else{
				commandExecutionStatus.setStatus(Status.FAILURE);
				commandExecutionStatus.setDescription("");
			}

		}
		return commandExecutionStatus;

	}

}
