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
import com.ericsson.fdp.business.enums.RSCommandOutputParamEnum;
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
 * This is a utility class that deals with RS commands.
 * 
 * @author Ericsson
 * 
 */
public class RSCommandUtil {

	/**
	 * Instantiates a new rS command util.
	 */
	private RSCommandUtil() {

	}

	/**
	 * This method is used to create a map of fully qualified path of the
	 * parameter and its value from the string representation of a xml for RS
	 * parameters.
	 * 
	 * @param outputParam
	 *            The string representation which is to be converted to the map.
	 * 
	 * @return The map containing fully qualified path and the value.
	 * @throws EvaluationFailedException
	 *             Exception, if in creating the map.
	 */
	public static Map<String, CommandParam> fromRSXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			if (outputParam.contains("GetServicesDtlsResponseTwo")) {
				final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("GetServicesDtlsResponseTwo");
				final Integer resultCode = Integer.parseInt(methodResponse.get("resultCode").toString());
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(resultCode);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put("resultCode".toLowerCase(), commandParamOutput);
				outputParams.putAll(getExtendedServiceDetailsParams(methodResponse, BusinessConstants.ROOT_TAG,
						BusinessConstants.SUB_TAG));
			} else if (outputParam.contains("GetServicesDtlsResponse")) {
				final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("GetServicesDtlsResponse");
				final Integer resultCode = Integer.parseInt(methodResponse.get("resultCode").toString());
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(resultCode);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put("resultCode".toLowerCase(), commandParamOutput);
				outputParams.putAll(getExtendedServiceDetailsParams(methodResponse, "servicesDtls", "service"));
			} else if(outputParam.contains("SingleResultDetails")){
				final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("SingleResultDetails");
				final Integer resultCode = Integer.parseInt(methodResponse.get("resultCode").toString());
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(resultCode);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put("resultCode".toLowerCase(), commandParamOutput);
			} else if(outputParam.contains("GetPinDetailResponse")){
				final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("GetPinDetailResponse");
				final Integer resultCode = Integer.parseInt(methodResponse.get("resultCode").toString());
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(resultCode);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put("resultCode".toLowerCase(), commandParamOutput);
			} else if(outputParam.contains("UpdatePinDetailResponse")){
				final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("UpdatePinDetailResponse");
				final Integer resultCode = Integer.parseInt(methodResponse.get("resultCode").toString());
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(resultCode);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put("resultCode".toLowerCase(), commandParamOutput);
			} else if(outputParam.contains("GetMe2USubscriberResponse")){
				final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("GetMe2USubscriberResponse");
				final Integer resultCode = Integer.parseInt(methodResponse.get("resultCode").toString());
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(resultCode);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put("resultCode".toLowerCase(), commandParamOutput);
			}
			else if(outputParam.contains("GetActiveBundlesDetailsResponse")){
				final JSONObject methodResponse = (JSONObject) xmlJSONObj.get("GetActiveBundlesDetailsResponse");
				final Integer resultCode = Integer.parseInt(methodResponse.get("resultCode").toString());
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(resultCode);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put("resultCode".toLowerCase(), commandParamOutput);
				//outputParams.putAll(setRSCommandOutputParams(methodResponse, "activebundles", "bundle"));
			}
			else
			{//used to handle if any other response is returned from RS 
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(FDPCommandConstants.RS_RESPONCODE_IFINVALIDRESPONSE);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put("resultCode".toLowerCase(), commandParamOutput);
			}
		} catch (final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	}

	/*
	 * private static Map<String, CommandParam> getServiceDetails( final
	 * JSONObject methodResponse) throws JSONException { final Map<String,
	 * CommandParam> outputParam = new HashMap<String, CommandParam>(); final
	 * JSONObject objectForMember = (JSONObject) methodResponse
	 * .get("servicesDtls"); if (objectForMember != null &&
	 * objectForMember.has("service")) { final Object serviceObject =
	 * objectForMember.get("service"); if (serviceObject instanceof JSONArray) {
	 * final JSONArray jsonArray = (JSONArray) serviceObject; for (int
	 * arrayIndex = 0; arrayIndex < jsonArray.length(); arrayIndex++) { final
	 * JSONObject arrayObject = (JSONObject) jsonArray .get(arrayIndex); String
	 * commonPath = "servicesDtls.service." + arrayIndex + ".";
	 * setCommandOutputParams(outputParam, arrayObject, "serviceId", commonPath,
	 * Primitives.STRING); } } else if (serviceObject instanceof JSONObject) {
	 * final JSONObject arrayObject = (JSONObject) serviceObject; String
	 * commonPath = "servicesDtls.service." + 0 + ".";
	 * setCommandOutputParams(outputParam, arrayObject, "serviceId", commonPath,
	 * Primitives.STRING); } } return outputParam; }
	 */
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
	 * This method will sert all the required output params
	 * 
	 * @param methodResponse
	 * @param rootTag
	 * @param subTag
	 * @return
	 * @throws JSONException
	 */
	private static Map<String, CommandParam> getExtendedServiceDetailsParams(final JSONObject methodResponse,
			final String rootTag, final String subTag) throws JSONException {
		final Map<String, CommandParam> outputParam = new HashMap<String, CommandParam>();
		final JSONObject objectForMember = (JSONObject) methodResponse.get(rootTag);
		if (objectForMember != null && objectForMember.has(subTag)) {
			final Object serviceObject = objectForMember.get(subTag);
			if (serviceObject instanceof JSONArray) {
				final JSONArray jsonArray = (JSONArray) serviceObject;
				for (int arrayIndex = 0; arrayIndex < jsonArray.length(); arrayIndex++) {
					final JSONObject arrayObject = (JSONObject) jsonArray.get(arrayIndex);
					//String commonPath = rootTag + BusinessConstants.DOT + subTag + BusinessConstants.DOT + arrayIndex + BusinessConstants.DOT;
					
					String commonPath = rootTag + BusinessConstants.DOT + arrayIndex
							+ BusinessConstants.DOT;
					
					setCommandParams(outputParam, arrayObject, commonPath);
				}
			} else if (serviceObject instanceof JSONObject) {
				final JSONObject arrayObject = (JSONObject) serviceObject;
				//String commonPath = rootTag + BusinessConstants.DOT + subTag + BusinessConstants.DOT + 0 + BusinessConstants.DOT;
				
				String commonPath = rootTag + BusinessConstants.DOT + 0
						+ BusinessConstants.DOT;
				
				setCommandParams(outputParam, arrayObject, commonPath);
			}
		}
		return outputParam;
	}

	/**
	 * This method will set all the required output params for Rs command
	 * 
	 * @param outputParam
	 * @param arrayObject
	 * @param commonPath
	 * @throws JSONException
	 */
	private static void setCommandParams(final Map<String, CommandParam> outputParam, final JSONObject arrayObject,
			String commonPath) throws JSONException {
		setCommandOutputParams(outputParam, arrayObject, "serviceId", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "renewalDate", commonPath, Primitives.DATETIME);
		setCommandOutputParams(outputParam, arrayObject, "activationDate", commonPath, Primitives.DATETIME);
		setCommandOutputParams(outputParam, arrayObject, "lastRenewalDate", commonPath, Primitives.DATETIME);
		setCommandOutputParams(outputParam, arrayObject, "renewalCount", commonPath, Primitives.LONG);
		setCommandOutputParams(outputParam, arrayObject, "productId", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "renewalPeriod", commonPath, Primitives.LONG);
		setCommandOutputParams(outputParam, arrayObject, "paySrc", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "splitAction", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "activationDate", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "price", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "srcChannel", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "activatedBy", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "OfferIds", commonPath, Primitives.STRING);
		setCommandOutputParams(outputParam, arrayObject, "DAs", commonPath, Primitives.STRING);		

	}

	/**
	 * This method is used to create string representation of xml for an RS
	 * command. The supported version is 5.0 for ACIP commands. The
	 * implementation will be changed to support a different version.
	 * 
	 * @param command
	 *            The command for which the xml representation is to be created.
	 * @return The string representation of the command.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toRSXmlFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		xmlFormat.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>").append("\n").append("<")
				.append(command.getCommandName()).append(">")
				.append(CGWCommandUtil.toCGWParametersXmlFormat(command.getInputParam())).append("</")
				.append(command.getCommandName()).append(">");
		return xmlFormat.toString();
	}

	/**
	 * This method creates the string representation of xml for RS parameters
	 * used in the command.
	 * 
	 * @param inputParam
	 *            The list of parameters for which the string representation is
	 *            to be created.
	 * @return The string representation.
	 * @throws ExecutionFailedException
	 *             Exception, if the xml could not be created.
	 */
	public static String toRSParametersXmlFormat(final List<CommandParam> inputParam) throws ExecutionFailedException {
		final StringBuilder xmlFormat = new StringBuilder();
		for (final CommandParam commandParam : inputParam) {
			xmlFormat.append(CommandParamUtil.toXmlForParam(commandParam));
		}
		return xmlFormat.toString();
	}

	/**
	 * This method is used to check if the RS command executed successfully or
	 * failed.
	 * 
	 * @param fdpCommand
	 *            The command to be checked.
	 * @return True, if command executed successfully, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception, if the status could not be evaluated.
	 */
	public static CommandExecutionStatus checkForRSCommandStatus(final FDPCommand fdpCommand)
			throws ExecutionFailedException {
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.RESPONSE_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandParam responseCodeParam = fdpCommand.getOutputParam(FDPCommandConstants.RS_RESPONSE_CODE_PATH);
		if (responseCodeParam != null) {
			isFailure = CommandUtil.checkResponseCode(configCache, responseCodeParam.getValue(),
					fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode((Integer) responseCodeParam.getValue());
			if (isFailure != null) {
				commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
				//setting write flag to true			
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
	 * This is generic method to set the RS command output parameters
	 * @param methodResponse
	 * @param rootTag
	 * @param subTag
	 * @return the param Map 
	 */
	private static Map<String, CommandParam> setRSCommandOutputParams(final JSONObject methodResponse,
			final String rootTag, final String subTag)  {
		
		final Map<String, CommandParam> outputParam = new HashMap<>();
		final JSONObject objectForMember = (JSONObject) methodResponse.get(rootTag);
		if (objectForMember != null && objectForMember.has(subTag)) {
			final Object serviceObject = objectForMember.get(subTag);
			if (serviceObject instanceof JSONArray) {
				final JSONArray jsonArray = (JSONArray) serviceObject;
				for (int arrayIndex = 0; arrayIndex < jsonArray.length(); arrayIndex++) {
					final JSONObject jsonObject = (JSONObject) jsonArray.get(arrayIndex);
					String commonPath = rootTag + BusinessConstants.DOT + arrayIndex
							+ BusinessConstants.DOT;		
				
					setRSCommandOutputParams(outputParam, jsonObject, commonPath);
				}
			} else if (serviceObject instanceof JSONObject) {
				final JSONObject jsonObject = (JSONObject) serviceObject;
				
				String commonPath = rootTag + BusinessConstants.DOT + 0
						+ BusinessConstants.DOT;				
				setRSCommandOutputParams(outputParam, jsonObject, commonPath);
			}
		}
		return outputParam;
	}
	
	/**
	 * This method would set the command output parameters in map based on available enum in RSCommandOutputParamEnum
	 * @param outputParam
	 * @param arrayObject
	 * @param commonPath
	 */
	private static void setRSCommandOutputParams(final Map<String, CommandParam> outputParam,
			final JSONObject jsonObject , final String commonPath) {
		for(RSCommandOutputParamEnum rsCommandParam : RSCommandOutputParamEnum.values())
			if(jsonObject.has(rsCommandParam.getParamName())){
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(jsonObject.get(rsCommandParam.getParamName()).toString());
				commandParamOutput.setPrimitiveValue(rsCommandParam.getPrimitive());
				outputParam.put((commonPath + rsCommandParam.getParamName()).toLowerCase(), commandParamOutput);
			}	
	}

}
