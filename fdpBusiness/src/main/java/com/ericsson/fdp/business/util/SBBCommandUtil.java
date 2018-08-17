package com.ericsson.fdp.business.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.FDPNodeAddInfoKeys;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.ExternalSystemDetail;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.SBBDetailUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;

public class SBBCommandUtil {

	/** The Constant KEY_VALUE_SEPERATOR. */
	private static final String KEY_VALUE_SEPERATOR = "=";

	/** The Constant PARAMETER_SEPERATOR. */
	private static final String PARAMETER_SEPERATOR = "&";

	/** The response Code */
	private static final String RESPONSE_CODE = "responseCode";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SBBCommandUtil.class);

	/**
	 * This method prepares the request to send to SBB.
	 * 
	 * @param command
	 *            the command
	 * @return the string
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static String toSBBFormat(final FDPCommand command)
			throws ExecutionFailedException {
		final StringBuilder sbbForamt = new StringBuilder();
		for (final CommandParam commandParam : command.getInputParam()) {
			sbbForamt.append(getParamString(commandParam)).append(
					PARAMETER_SEPERATOR);
		}
		return sbbForamt.length() > PARAMETER_SEPERATOR.length() ? sbbForamt
				.substring(0,
						sbbForamt.length() - PARAMETER_SEPERATOR.length())
				: null;
	}

	/**
	 * This method gets the SBB External System Details from Cache.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the external system
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static ExternalSystemDetail getExternalSystem(
			final FDPRequest fdpRequest) throws ExecutionFailedException {
		ExternalSystemDetail externalSystemDetail = null;

		String logicalName = (String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME);
		logicalName = (null == logicalName) ? getLogicalNameFromNode(fdpRequest) : logicalName;
		logicalName = (null == logicalName) ? getDefaultSBBLogicalNameFromCircleConfig(fdpRequest) : logicalName;
		if (null == logicalName) {
			throw new ExecutionFailedException(
					"EXTERNAL_SYSTEM_LOGICAL_NAME not set in request");
		}
		LOGGER.debug(
				"AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME value in request is {} for requestId {}.",
				logicalName, fdpRequest.getRequestId());

		final FDPAppBag key = new FDPAppBag(AppCacheSubStore.SBB_DETAILS,
				SBBDetailUtil.getEndPoint(fdpRequest.getCircle()
						.getCircleCode(), logicalName));

		LOGGER.debug("SBB cache key is {} for requestId {}.", key,
				fdpRequest.getRequestId());
		FDPCacheable cacheable = (FDPCacheable) ApplicationConfigUtil
				.getApplicationConfigCache().getValue(key);
		if (null != cacheable && cacheable instanceof ExternalSystemDetail) {
			externalSystemDetail = (ExternalSystemDetail) cacheable;
		} else {
			final String defaultLogicalName = getDefaultSBBLogicalNameFromCircleConfig(fdpRequest);
			final FDPAppBag keyDefaultLogicalName = new FDPAppBag(AppCacheSubStore.SBB_DETAILS,
					SBBDetailUtil.getEndPoint(fdpRequest.getCircle()
							.getCircleCode(), defaultLogicalName));
			cacheable = (FDPCacheable) ApplicationConfigUtil
					.getApplicationConfigCache().getValue(keyDefaultLogicalName);
			LOGGER.debug("SBB Default cache key is {} for requestId {}.", keyDefaultLogicalName,
					fdpRequest.getRequestId());
			if(null != cacheable && cacheable instanceof ExternalSystemDetail) {
				externalSystemDetail = (ExternalSystemDetail) cacheable;
			}
		}

		// If No Object found from Cache then throw exception.
		if (null == externalSystemDetail) {
			throw new ExecutionFailedException(
					"Unable to get ExternalSystemDetails for SBB");
		}
		return externalSystemDetail;
	}

	/**
	 * This method check the command status.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @return the command execution status
	 */
	public static CommandExecutionStatus checkForSBBCommandStatus(
			final FDPCommand fdpCommand) throws ExecutionFailedException {
		final FDPCache<FDPAppBag, Object> configCache = ApplicationConfigUtil.getApplicationConfigCache();
		FDPResultCodesDTO isFailure = null;
		final CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.FAILURE, 0,
				FDPConstant.ERROR_CODE_MAPPING_MISSING, ErrorTypes.FAULT_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.valueOf(fdpCommand.getSystem().name()));
		final CommandParam commandParam = fdpCommand
				.getOutputParam(RESPONSE_CODE.toLowerCase());
		if(null != commandParam ) {
			isFailure = CommandUtil.checkResponseCode(configCache, commandParam.getValue(),
					fdpCommand.getCommandDisplayName());
			commandExecutionStatus.setCode((Integer) commandParam.getValue());
			commandExecutionStatus.setErrorType(ErrorTypes.RESPONSE_CODE.name());
			if (isFailure != null) {
				commandExecutionStatus.setDescription(isFailure.getResultCodeDesc());
			}
		}
		commandExecutionStatus.setStatus(CommandUtil.getStatus(isFailure));
		return commandExecutionStatus;
	}

	/**
	 * This method prepares the command parameters into SBB Required
	 * Format.
	 * 
	 * @param commandParam
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static String getParamString(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder sbbFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PRIMITIVE:
			sbbFormat.append(commandParam.getName())
					.append(KEY_VALUE_SEPERATOR)
					.append(commandParam.getValue().toString());
			break;
		default:
			throw new ExecutionFailedException(
					"The command parameter type is not recognized. It is of type "
							+ commandParam.getType());
		}
		return sbbFormat.toString();
	}
	
	/**
	 * This method lookups the logical name from node directly.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private static String getLogicalNameFromNode(final FDPRequest fdpRequest) {
		String logicalName = null;
		Object object = fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if(null != object && object instanceof AbstractNode) {
			AbstractNode abstractNode = (AbstractNode) object;
			logicalName = (String) abstractNode.getAdditionalInfo(FDPNodeAddInfoKeys.EXTERNAL_SYSTEM_LOGICAL_NAME.name());
			LOGGER.debug(
					"AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME value in node is {} for requestId {}.",
					logicalName, fdpRequest.getRequestId());
		}
		return logicalName; 
	}
	
	/**
	 * This method will get the default SBB Logical Name.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private static String getDefaultSBBLogicalNameFromCircleConfig(final FDPRequest fdpRequest) {
		return fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.SBB_EXTERNAL_SYSTEM_FULFILLEMENT_LOGICAL_NAME.getAttributeName()).toString();
	}
	
	/**
	 * This method will parse the xml from SBB as per FDP.
	 * 
	 * @param outputParam
	 * @return
	 * @throws EvaluationFailedException
	 */
	public static Map<String, CommandParam> fromSBBXmlToParameters(final String outputParam)
			throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		try {
			final JSONObject xmlJSONObj = XML.toJSONObject(outputParam);
			if (null != outputParam && outputParam.contains("response") && null != xmlJSONObj) {
				final JSONObject response = (JSONObject) xmlJSONObj.get("response");
				final Integer resultCode = Integer.parseInt(response.get("responseCode").toString());
				final CommandParamOutput commandParamOutput = new CommandParamOutput();
				commandParamOutput.setValue(resultCode);
				commandParamOutput.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put(RESPONSE_CODE.toLowerCase(), commandParamOutput);
				updateCommandOutPutParam(xmlJSONObj, "responseDesc", outputParams, "responseDesc");
				updateCommandOutPutParam(xmlJSONObj, "responseStatus", outputParams, "responseStatus");
				outputParams.putAll(updateCommandOutPutParamForData(response));
			}
		} catch (final JSONException e) {
			throw new EvaluationFailedException("The input xml is not valid.", e);
		}
		return outputParams;
	}
	
	/**
	 * This method will update the output params.
	 * 
	 * @param xmlJSONObj
	 * @param xmlParamter
	 * @param outputParams
	 * @throws JSONException
	 */
	private static void updateCommandOutPutParam(final JSONObject xmlJSONObj, final String xmlParamter,
			final Map<String, CommandParam> outputParams, final String key) throws JSONException {
		final JSONObject response = (JSONObject) xmlJSONObj.get("response");
		final String responseDesc = response.get(xmlParamter).toString();
		final CommandParamOutput commandParamOutputRespDesc = new CommandParamOutput();
		commandParamOutputRespDesc.setValue(responseDesc);
		commandParamOutputRespDesc.setPrimitiveValue(Primitives.STRING);
		outputParams.put(key.toLowerCase(), commandParamOutputRespDesc);
	}
	
	/**
	 * This  method will update the Data .
	 * 
	 * @return
	 * @throws JSONException 
	 */
	private static Map<String, CommandParam> updateCommandOutPutParamForData(final JSONObject xmlJSONObj)
			throws JSONException {
		Map<String, CommandParam> commandOutPuts = new HashMap<String, CommandParam>();
		if (xmlJSONObj.has("data")) {
			final JSONObject data = (JSONObject) xmlJSONObj.get("data");
			if (null != data && data.has("consumerList")) {
				final String consumerList = data.getString("consumerList");
				final CommandParamOutput commandParamconsumerList = new CommandParamOutput();
				commandParamconsumerList.setValue(consumerList);
				commandParamconsumerList.setPrimitiveValue(Primitives.STRING);
				commandOutPuts.put("consumerList".toLowerCase(), commandParamconsumerList);
			}
			if (null != data && data.has("provider")) {
				final String provider = data.getString("provider");
				final CommandParamOutput commandParamProvider = new CommandParamOutput();
				commandParamProvider.setValue(provider);
				commandParamProvider.setPrimitiveValue(Primitives.STRING);
				commandOutPuts.put("provider".toLowerCase(), commandParamProvider);
			}
			if (null != data && data.has("type")) {
				final String type = data.getString("type");
				final CommandParamOutput commandParamType = new CommandParamOutput();
				commandParamType.setValue(type);
				commandParamType.setPrimitiveValue(Primitives.STRING);
				commandOutPuts.put("type".toLowerCase(), commandParamType);
			}
		}
		return commandOutPuts;
	}
}
