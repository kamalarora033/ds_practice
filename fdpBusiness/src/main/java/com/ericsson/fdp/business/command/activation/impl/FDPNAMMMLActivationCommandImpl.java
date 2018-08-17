package com.ericsson.fdp.business.command.activation.impl;

import com.ericsson.fdp.business.constants.MMLCommandConstants;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ThreeGActivationCommandMode;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class provides interceptor functionality for attaching NAM command.
 * 
 * @author Ericsson
 * 
 */
public class FDPNAMMMLActivationCommandImpl extends FDPDefaultActivationCommandImpl {

	/**
	 * The value when Two G is already activated.
	 */
	private static final String TWO_G_ACTIVATED_VALUE = "0";

	/**
	 * The constructor.
	 * 
	 * @param fdpCommand
	 *            the command to be intercepted.
	 */
	public FDPNAMMMLActivationCommandImpl(final FDPCommand fdpCommand) {
		super(fdpCommand);
	}

	@Override
	protected boolean preProcess(final FDPRequest fdpRequest) throws ExecutionFailedException {
		// Check if the output contains NAM as 0 then do not proceed.
		boolean response = false;
		if (!checkForNAM(fdpRequest)) {
			response = super.preProcess(fdpRequest);
		}
		return response;
	}

	/**
	 * This method is used to check if NAM is already activated for the user.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @return true, if NAM already attached, false otherwise.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private boolean checkForNAM(final FDPRequest fdpRequest) throws ExecutionFailedException {
		boolean activated = false;
		final ThreeGActivationCommandMode threeGActivationCommandMode = (ThreeGActivationCommandMode) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_MODE);
		if (ThreeGActivationCommandMode.QUERY_AND_UPDATE_ALL.equals(threeGActivationCommandMode)) {
			final FDPCommand fdpCommand = fdpRequest.getExecutedCommand(ThreeGActivationCommandMode.QUERY
					.getCommandsForMMLThreeG().get(0));
			if (fdpCommand == null) {
				throw new ExecutionFailedException("The query command has not been executed for query and update case.");
			}
			final CommandParam namCommandParam = fdpCommand.getOutputParam(MMLCommandConstants.NAM_VALUE);
			if (namCommandParam != null) {
				final String namValue = (String) namCommandParam.getValue();
				if (namValue.equals(TWO_G_ACTIVATED_VALUE)) {
					activated = true;
				}
			}
		}
		return activated;
	}
}
