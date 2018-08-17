package com.ericsson.fdp.business.adapter;

import java.util.Map;

import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * This interface provides the method to call the client.
 * 
 * @author Ericsson
 * 
 */
public interface Adapter {

	/**
	 * This method invokes the appropriate client and sends the response.
	 * 
	 * @return The return object from calling the client. The expected keys are
	 *         RESPONSE_CODE and COMMAND_OUTPUT with the other values.
	 * @throws ExecutionFailedException
	 * @throws
	 */
	Map<String, Object> callClient() throws ExecutionFailedException;

}
