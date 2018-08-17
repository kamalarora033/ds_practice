package com.ericsson.fdp.business.request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.request.ResponseError;


/**
 * The Class FDPCheckConsumerResponseImpl.
 */
public class FDPCheckConsumerResponseImpl implements FDPCheckConsumerResponse {


	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2622265728016877464L;

	/** The pre paid consumer. */
	private boolean prePaidConsumer = false;
	
	/** The response error. */
	private ResponseError responseError = null;
	
	/** The status. */
	private Status status = Status.FAILURE;
	
	/**
	 * The list of commands that have been fired.
	 */
	private List<FDPCommand> executedCommands;
	
	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Sets the status.
	 *
	 * @param status the new status
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * Sets the response error.
	 *
	 * @param responseError the new response error
	 */
	public void setResponseError(ResponseError responseError) {
		this.responseError = responseError;
	}

	@Override
	public boolean isPrePaidConsumer() {
		return prePaidConsumer;
	}

	@Override
	public Status getExecutionStatus() {
		return status;
	}


	@Override
	public ResponseError getResponseError() {
		
		return responseError;
	}

	/**
	 * Sets the pre paid consumer.
	 *
	 * @param prePaidConsumer the new pre paid consumer
	 */
	public void setPrePaidConsumer(boolean prePaidConsumer) {
		this.prePaidConsumer = prePaidConsumer;
	}
	
	
	public void addExecutedCommand(FDPCommand fdpCommand) {
		if (executedCommands == null) {
			executedCommands = new ArrayList<FDPCommand>(FDPConstant.DEFAULT_INTIAL_CAPACITY_COMMAND);
		}
		executedCommands.add(fdpCommand);
	}

	@Override
	public List<FDPCommand> getExecutedCommands() {
		return executedCommands;
	}

	
}
