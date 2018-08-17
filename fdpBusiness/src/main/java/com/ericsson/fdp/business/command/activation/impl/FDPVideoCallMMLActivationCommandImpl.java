package com.ericsson.fdp.business.command.activation.impl;

import com.ericsson.fdp.business.constants.MMLCommandConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The interceptor for the video call activation command.
 * 
 * @author Ericsson
 * 
 */
public class FDPVideoCallMMLActivationCommandImpl extends FDPDefaultActivationCommandImpl {
	/**
	 * The string to identify if video call is already activated.
	 */
	private static final String VIDEO_CALL_ACTIVATED = "BS3G-1";

	/**
	 * The constructor.
	 * 
	 * @param fdpCommand
	 *            the command to intercept.
	 */
	public FDPVideoCallMMLActivationCommandImpl(final FDPCommand fdpCommand) {
		super(fdpCommand);
	}

	@Override
	protected boolean preProcess(final FDPRequest fdpRequest) throws ExecutionFailedException {
		boolean response = false;
		final boolean icrQualified = checkForICRCircleQualified(fdpRequest);
		if (icrQualified && checkIfCommandNeedsExecution(fdpRequest)) {
			response = super.preProcess(fdpRequest);
		}
		return response;
	}

	/**
	 * This method checks if the command needs execution.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @return true, if command is to be executed, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private boolean checkIfCommandNeedsExecution(final FDPRequest fdpRequest) throws ExecutionFailedException {
		boolean needsExecution = true;
		final ThreeGActivationCommandMode threeGActivationCommandMode = (ThreeGActivationCommandMode) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_MODE);
		if (ThreeGActivationCommandMode.QUERY_AND_UPDATE_ALL.equals(threeGActivationCommandMode)) {
			final FDPCommand fdpCommand = fdpRequest.getExecutedCommand(ThreeGActivationCommandMode.QUERY
					.getCommandsForMMLThreeG().get(0));
			if (fdpCommand == null) {
				throw new ExecutionFailedException("The query command has not been executed for query and update case.");
			}
			final String responseValue = fdpCommand.getOutputParam(MMLCommandConstants.RESPONSE).getValue().toString();
			if (responseValue.contains(VIDEO_CALL_ACTIVATED)) {
				needsExecution = false;
			}
		}
		return needsExecution;
	}

}
