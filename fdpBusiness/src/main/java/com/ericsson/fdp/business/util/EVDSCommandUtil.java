package com.ericsson.fdp.business.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.constants.BusinessConstants;
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

public class EVDSCommandUtil {
	
	/**
	 * Instantiates a new uCIP command util.
	 */
	private EVDSCommandUtil() {

	}
	
	/**
	 * This method is used to create string representation of xml for an EVDS
	 * command.
	 * 
	 * @param command
	 *            The command for which the xml representation is to be created.
	 * @return The string representation of the command.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toEVDSXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
				 .append("<agiml>")
				 .append("<header>")
				 .append("<interface>").append(command.getCommandName()).append("</interface>")
				 .append("</header>")
				 .append("<process>")
				 .append(EVDSCommandUtil.toEVDSParametersXmlFormat(command.getInputParam()))
				 .append("</process>")
				 .append("</agiml>");	
		return xmlFormat.toString();
	}

	/**
	 * This method creates the string representation of xml for EVDS parameters
	 * used in the command.
	 * 
	 * @param inputParam
	 *            The list of parameters for which the string representation is
	 *            to be created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	private static Object toEVDSParametersXmlFormat(List<CommandParam> inputParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		for (final CommandParam commandParam : inputParam) {
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
		final String paramName = commandParam.getName().toString();
		xmlFormat.append("<").append(paramName).append(">").append(commandParam.getValue().toString()).append("</")
		.append(paramName).append(">");
		return xmlFormat.toString();
	}

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for EVDS
	 * parameters.
	 * 
	 * @param outputParam
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 */
	public static Map<String, CommandParam> fromEvdsXmltoParameter(String outputXmlAsString) throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		try {
			final JSONObject xmlJSONObj = (JSONObject) XML.toJSONObject(outputXmlAsString).get("agiml");
			final JSONObject response = (JSONObject) xmlJSONObj.get("response");
			final Integer resultCode = Integer.parseInt(response.get("resultcode").toString());
			final CommandParamOutput commandParamOutput = new CommandParamOutput();
			commandParamOutput.setValue(resultCode);
			commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
			outputParams.put("resultCode".toLowerCase(), commandParamOutput);
			outputParams.putAll(getWARecordParams(response, "records", "record"));
		}catch(final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	}	
	
	/**
	 * This method will sert all the required output params
	 * 
	 * @param methodResponse
	 * @param rootTag
	 * @param subTag
	 * @return
	 * @throws JSONException
	 */
	private static Map<String, CommandParam> getWARecordParams(final JSONObject methodResponse,
			final String rootTag, final String subTag) throws JSONException {
		final Map<String, CommandParam> outputParam = new HashMap<String, CommandParam>();
		final JSONObject objectForMember = (JSONObject) methodResponse.get(rootTag);
		if (objectForMember != null && objectForMember.has(subTag)) {
			final Object recordObject = objectForMember.get(subTag);
			if (recordObject instanceof JSONArray) {
				final JSONArray jsonArray = (JSONArray) recordObject;
				for (int i = 0; i < jsonArray.length(); i++) {
					final JSONObject arrayObject = (JSONObject) jsonArray.get(i);
					String commonPath = rootTag + BusinessConstants.DOT + subTag + BusinessConstants.DOT + i + BusinessConstants.DOT;
					setCommandParams(outputParam, arrayObject, commonPath);
				}
			} else if (recordObject instanceof JSONObject) {
				final JSONObject arrayObject = (JSONObject) recordObject;
				String commonPath = rootTag + BusinessConstants.DOT + subTag + BusinessConstants.DOT + 0
						+ BusinessConstants.DOT;
				setCommandParams(outputParam, arrayObject, commonPath);
			}
		}
		return outputParam;
	}
	
	/**
	 * This method will set all the required output params for EVDS Wallet Adjustment command
	 * 
	 * @param outputParam
	 * @param arrayObject
	 * @param commonPath
	 * @throws JSONException
	 */
	private static void setCommandParams(final Map<String, CommandParam> outputParam, final JSONObject arrayObject,
			String commonPath) throws JSONException {
		setCommandOutputParams(outputParam, arrayObject, "value", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "amount", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "taxvalue", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "agentid", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "transtypeid", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "reasonid", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "cts", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "operatorid", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "statusid", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "walletadjustmentid", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "commissionvalue", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "commissionpercent", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "commissiontax", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "destinationwalletid", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "sourcewalletid", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "transid", commonPath, Primitives.STRING);
	}
	
	/**
	 * This method will set the output param with the name provided in paramName
	 * and for path commonPath in xml output
	 * 
	 * @param outputParam
	 * @param arrayIndex
	 * @param arrayObject
	 * @param paramName
	 * @param commonPath
	 * @throws JSONException
	 */
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
	 * This method is used to check if the EVDS command executed successfully or
	 * failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if the status could not be evaluated.
	 */
	public static CommandExecutionStatus checkForEVDSCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		final CommandParam responseCodeParam = fdpCommand.getOutputParam(FDPCommandConstants.EVDS_RESPONSE_CODE_PATH);
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
		if(isFailure==null)
			((AbstractCommand)fdpCommand).setWriteToFailureFile(true);
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		return commandExecutionStatus;

	}
	
}
