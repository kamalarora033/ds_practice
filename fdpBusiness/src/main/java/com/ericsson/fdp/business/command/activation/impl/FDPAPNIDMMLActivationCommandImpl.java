package com.ericsson.fdp.business.command.activation.impl;

import java.util.List;
import java.util.Map;

import com.ericsson.fdp.business.constants.MMLCommandConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used as an interceptor for APNID command.
 * 
 * @author Ericsson
 * 
 */
public class FDPAPNIDMMLActivationCommandImpl extends FDPDefaultActivationCommandImpl {

	/**
	 * The constructor for the command.
	 * 
	 * @param fdpCommand
	 *            the command.
	 */
	public FDPAPNIDMMLActivationCommandImpl(final FDPCommand fdpCommand) {
		super(fdpCommand);
	}

	@Override
	protected boolean preProcess(final FDPRequest fdpRequest) throws ExecutionFailedException {
		// Check if the output contains EQOSID, APNID, PDPID and the same is
		// defined for the circle then do not execute.
		boolean response = false;
		if (!checkForValuesMatch(fdpRequest)) {
			response = super.preProcess(fdpRequest);
		}
		return response;
	}

	/**
	 * This method is used to validate if command is to be executed or not.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @return true if values match, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	@SuppressWarnings("unchecked")
	private boolean checkForValuesMatch(final FDPRequest fdpRequest) throws ExecutionFailedException {
		boolean valuesMatch = false;
		final ThreeGActivationCommandMode threeGActivationCommandMode = (ThreeGActivationCommandMode) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_MODE);
		if (ThreeGActivationCommandMode.QUERY_AND_UPDATE_ALL.equals(threeGActivationCommandMode)) {
			final FDPCommand fdpCommand = fdpRequest.getExecutedCommand(ThreeGActivationCommandMode.QUERY
					.getCommandsForMMLThreeG().get(0));
			if (fdpCommand == null) {
				throw new ExecutionFailedException("The query command has not been executed for query and update case.");
			}
			final CommandParam commandParam = fdpCommand
					.getOutputParam(MMLCommandConstants.PACKET_DATA_PROTOCOL_CONTEXT_DATA);
			if (commandParam != null) {
				final List<Map<String, String>> packetDataVal = (List<Map<String, String>>) commandParam.getValue();
				for (final Map<String, String> map : packetDataVal) {
					if (checkForValuesMatch(fdpRequest, ConfigurationKey.APNID, map.get(MMLCommandConstants.APNID))
							&& checkForValuesMatch(fdpRequest, ConfigurationKey.EQOSID,
									map.get(MMLCommandConstants.EQOSID))
							&& checkForValuesMatch(fdpRequest, ConfigurationKey.PDPID,
									map.get(MMLCommandConstants.PDPID))) {
						valuesMatch = true;
					}
				}
			}
		}
		return valuesMatch;
	}

	/**
	 * This method checks if the values for the circle match.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @param configurationKey
	 *            the configuration key to be used.
	 * @param responseFound
	 *            the response found.
	 * @return true if the values match for the circle.
	 */
	private boolean checkForValuesMatch(final FDPRequest fdpRequest, final ConfigurationKey configurationKey,
			final String responseFound) {
		boolean valuesMatch = false;
		String circleConfig = null;
		if (responseFound != null) {
			circleConfig = fdpRequest.getCircle().getConfigurationKeyValueMap()
					.get(configurationKey.getAttributeName());
			if (circleConfig != null && circleConfig.equals(responseFound)) {
				valuesMatch = true;
			}
		}
		FDPLogger.debug(LoggerUtil.getSummaryLoggerFromRequest(fdpRequest), this.getClass(), "checkForValuesMatch",
				"Pre processing for APNID, checking " + configurationKey + " with " + responseFound
						+ " with circle value " + circleConfig + " resultant is " + valuesMatch);
		return valuesMatch;
	}
}
