package com.ericsson.fdp.business.command.activation.impl;

import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The interceptor for the video call activation command.
 * 
 * @author Ericsson
 * 
 */
public class FDPCAIThreeGActivationCommandImpl extends FDPDefaultActivationCommandImpl {
	/**
	 * The constructor.
	 * 
	 * @param fdpCommand
	 *            the command to intercept.
	 */
	public FDPCAIThreeGActivationCommandImpl(final FDPCommand fdpCommand) {
		super(fdpCommand);
	}

	@Override
	protected boolean preProcess(final FDPRequest fdpRequest) throws ExecutionFailedException {
		boolean response = false;
		final boolean icrQualified = checkForICRCircleQualified(fdpRequest);
		if (icrQualified) {
			response = super.preProcess(fdpRequest);
		}
		return response;
	}

}
