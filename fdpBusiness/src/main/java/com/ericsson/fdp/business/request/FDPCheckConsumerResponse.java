package com.ericsson.fdp.business.request;

import java.io.Serializable;
import java.util.List;

import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.request.ResponseError;

/**
 * The Interface FDPCheckConsumerResponse.
 * 
 * @author Ericsson
 */
public interface FDPCheckConsumerResponse extends Serializable{

	/**
	 * Checks if is pre paid consumer.
	 * 
	 * @return true, if is pre paid consumer
	 */
	public boolean isPrePaidConsumer();

	/**
	 * Gets the execution status.
	 *
	 * @return the execution status
	 */
	Status getExecutionStatus();

	/**
	 * Gets the response error.
	 *
	 * @return the response error
	 */
	ResponseError getResponseError();

	/**
	 * Gets the executed command.
	 *
	 * @param commandName the command name
	 * @return the executed command
	 */
	List<FDPCommand> getExecutedCommands();

}
