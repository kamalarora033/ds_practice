package com.ericsson.fdp.business.util;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.command.param.impl.CommandParamOutput;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ErrorTypes;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.enums.FDPNodeAddInfoKeys;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.entity.ExternalSystemDetail;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.MCarbonDetailUtil;
import com.ericsson.fdp.dao.enums.CommandParameterType;

/**
 * The Class MCarbonCommandUtil.
 * 
 * @author Ericsson
 */
public final class MCarbonCommandUtil {

	/** The Constant KEY_VALUE_SEPERATOR. */
	private static final String KEY_VALUE_SEPERATOR = "=";

	/** The Constant PARAMETER_SEPERATOR. */
	private static final String PARAMETER_SEPERATOR = "&";

	/** The Constant RESULT_CODE_VALUE. */
	private static final String RESULT_CODE_VALUE = "RESULT_CODE_VALUE";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MCarbonCommandUtil.class);

	/**
	 * Instantiates a new m carbon command util.
	 */
	private MCarbonCommandUtil() {

	}

	/**
	 * To m carbon format.
	 * 
	 * @param command
	 *            the command
	 * @return the string
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static String toMCarbonFormat(final FDPCommand command) throws ExecutionFailedException {
		final StringBuilder mCarbonFormat = new StringBuilder();
		for (final CommandParam commandParam : command.getInputParam()) {
			mCarbonFormat.append(getParamString(commandParam)).append(PARAMETER_SEPERATOR);
		}
		return mCarbonFormat.length() > PARAMETER_SEPERATOR.length() ? mCarbonFormat.substring(0,
				mCarbonFormat.length() - PARAMETER_SEPERATOR.length()) : null;
	}

	/**
	 * Gets the param string.
	 * 
	 * @param commandParam
	 *            the command param
	 * @return the param string
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private static String getParamString(final CommandParam commandParam) throws ExecutionFailedException {
		final StringBuilder mCarbonFormat = new StringBuilder();
		switch (commandParam.getType()) {
		case PRIMITIVE:
			mCarbonFormat.append(commandParam.getName()).append(KEY_VALUE_SEPERATOR)
					.append(commandParam.getValue().toString());
			break;
		default:
			throw new ExecutionFailedException("The command parameter type is not recognized. It is of type "
					+ commandParam.getType());
		}
		return mCarbonFormat.toString();
	}

	/**
	 * Gets the external system.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the external system
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static ExternalSystemDetail getExternalSystem(final FDPRequest fdpRequest) throws ExecutionFailedException {
		ExternalSystemDetail externalSystemDetail = null;
		String logicalName = (String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME);
		LOGGER.debug("AuxRequestParam.EXTERNAL_SYSTEM_LOGICAL_NAME value in request is {} for requestId {}.",
				logicalName, fdpRequest.getRequestId());
		if(null == logicalName) {
			logicalName = getLogicalNameFromNode(fdpRequest);
		}
		final FDPAppBag key = new FDPAppBag(AppCacheSubStore.MCARBON_DETAILS, MCarbonDetailUtil.getEndPoint(fdpRequest
				.getCircle().getCircleCode(), logicalName));
		LOGGER.debug("MCARBON cache key is {} for requestId {}.", key, fdpRequest.getRequestId());
		final FDPCacheable cacheable = (FDPCacheable) ApplicationConfigUtil.getApplicationConfigCache().getValue(key);
		if (cacheable instanceof ExternalSystemDetail) {
			externalSystemDetail = (ExternalSystemDetail) cacheable;
		}
		return externalSystemDetail;
	}

	/**
	 * Check for m carbon command status.
	 * 
	 * @param fdpCommand
	 *            the fdp command
	 * @return the command execution status
	 */
	public static CommandExecutionStatus checkForMCarbonCommandStatus(final FDPCommand fdpCommand) {
		return new CommandExecutionStatus(Status.SUCCESS, 0, "SUCCESS", ErrorTypes.RESPONSE_CODE.name(),
				com.ericsson.fdp.common.enums.ExternalSystem.MCARBON);
	}

	/**
	 * From m carbon to parameters.
	 * 
	 * @param outputXmlAsString
	 *            the output xml as string
	 * @return the map
	 */
	public static Map<String, CommandParam> fromMCarbonToParameters(final String outputXmlAsString) {
		final Map<String, CommandParam> outputParams = new LinkedHashMap<String, CommandParam>();
		final CommandParamOutput fdpCommandParamOutput = new CommandParamOutput();
		fdpCommandParamOutput.setPrimitiveValue(Primitives.STRING);
		fdpCommandParamOutput.setType(CommandParameterType.PRIMITIVE);

		fdpCommandParamOutput.setValue(outputXmlAsString);
		outputParams.put(RESULT_CODE_VALUE, fdpCommandParamOutput);
		return outputParams;
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
