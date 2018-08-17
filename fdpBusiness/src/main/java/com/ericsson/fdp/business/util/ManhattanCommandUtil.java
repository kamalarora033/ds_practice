package com.ericsson.fdp.business.util;

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
import com.ericsson.fdp.core.utils.ManhattanDetailUtil;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;

/**
 * The Class ManhattanCommandUtil.
 * 
 * @author Ericsson
 */
public final class ManhattanCommandUtil {

	/** The Constant KEY_VALUE_SEPERATOR. */
	private static final String KEY_VALUE_SEPERATOR = "=";

	/** The Constant PARAMETER_SEPERATOR. */
	private static final String PARAMETER_SEPERATOR = "&";

	/** The output */
	private static final String RESPONSE_OUTPUT = "output";

	/** The response Code */
	private static final String RESPONSE_CODE = "responseCode";

	/** The response msg */
	public static final String RESPONSE_MSG = "responseMsg";
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ManhattanCommandUtil.class);

	/**
	 * This method prepares the request to send to Manhattan.
	 * 
	 * @param command
	 *            the command
	 * @return the string
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static String toManhattanFormat(final FDPCommand command)
			throws ExecutionFailedException {
		final StringBuilder manhattanForamt = new StringBuilder();
		for (final CommandParam commandParam : command.getInputParam()) {
			manhattanForamt.append(getParamString(commandParam)).append(
					PARAMETER_SEPERATOR);
		}
		return manhattanForamt.length() > PARAMETER_SEPERATOR.length() ? manhattanForamt
				.substring(0,
						manhattanForamt.length() - PARAMETER_SEPERATOR.length())
				: null;
	}

	/**
	 * This method gets the Manhattan External System Details from Cache.
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
		if (null == logicalName) {
			throw new ExecutionFailedException(
					"EXTERNAL_SYSTEM_LOGICAL_NAME not set in request");
		}
		LOGGER.debug(
				"AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME value in request is {} for requestId {}.",
				logicalName, fdpRequest.getRequestId());

		final FDPAppBag key = new FDPAppBag(AppCacheSubStore.MANHATTAN_DETAILS,
				ManhattanDetailUtil.getEndPoint(fdpRequest.getCircle()
						.getCircleCode(), logicalName));

		LOGGER.debug("MANHATTAN cache key is {} for requestId {}.", key,
				fdpRequest.getRequestId());
		final FDPCacheable cacheable = (FDPCacheable) ApplicationConfigUtil
				.getApplicationConfigCache().getValue(key);
		if (null != cacheable && cacheable instanceof ExternalSystemDetail) {
			externalSystemDetail = (ExternalSystemDetail) cacheable;
		}

		// If No Object found from Cache then throw exception.
		if (null == externalSystemDetail) {
			throw new ExecutionFailedException(
					"Unable to get ExternalSystemDetails for MANHATTAN");
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
	public static CommandExecutionStatus checkForManhattanCommandStatus(
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
	 * This method parse the xml response from Manhattan.
	 * 
	 * @param outputXmlAsString
	 *            the output xml as string
	 * @return the map
	 */
	public static Map<String, CommandParam> fromManhattanToParameters(
			final String outputXmlAsString) throws EvaluationFailedException {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		try {
			if (null != outputXmlAsString
					&& outputXmlAsString.contains(RESPONSE_OUTPUT)) {
				final JSONObject xmlJSONObj = XML
						.toJSONObject(outputXmlAsString);
				final JSONObject methodResponse = (JSONObject) xmlJSONObj
						.get(RESPONSE_OUTPUT);
				final Integer responseCode = Integer.valueOf(methodResponse
						.get(RESPONSE_CODE).toString());
				final String responseMsg = String.valueOf(methodResponse
						.get(RESPONSE_MSG));
				// The Command Response Code
				final CommandParamOutput commandResponseCode = new CommandParamOutput();
				commandResponseCode.setValue(responseCode);
				commandResponseCode.setPrimitiveValue(Primitives.INTEGER);
				outputParams.put(RESPONSE_CODE.toLowerCase(),
						commandResponseCode);
				// The command Response Message
				final CommandParamOutput commandResponseMsg = new CommandParamOutput();
				commandResponseMsg.setValue(responseMsg);
				commandResponseMsg.setPrimitiveValue(Primitives.STRING);
				outputParams
						.put(RESPONSE_MSG.toLowerCase(), commandResponseMsg);
				LOGGER.debug("MANHATTAN commandOutParams Map {}", outputParams);
			}
		} catch (JSONException e) {
			throw new EvaluationFailedException(
					"Unable to parse response xml for Manhattan, XML:["
							+ outputXmlAsString + "]", e);
		}
		return outputParams;
	}

	/**
	 * This method prepares the command parameters into Manhattan Required
	 * Format.
	 * 
	 * @param commandParam
	 * @return
	 * @throws ExecutionFailedException
	 */
	private static String getParamString(final CommandParam commandParam)
			throws ExecutionFailedException {
		final StringBuilder manhattanFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PRIMITIVE:
			manhattanFormat.append(commandParam.getName())
					.append(KEY_VALUE_SEPERATOR)
					.append(commandParam.getValue().toString());
			break;
		default:
			throw new ExecutionFailedException(
					"The command parameter type is not recognized. It is of type "
							+ commandParam.getType());
		}
		return manhattanFormat.toString();
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
}
