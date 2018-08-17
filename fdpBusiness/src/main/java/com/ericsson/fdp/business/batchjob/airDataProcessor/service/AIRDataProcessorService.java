package com.ericsson.fdp.business.batchjob.airDataProcessor.service;

import java.net.UnknownHostException;

import javax.ejb.Remote;

import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * The Interface AIRDataProcessorService.
 */
@Remote
public interface AIRDataProcessorService {

	/**
	 * This method save batch execution info for air offline records.
	 * 
	 * @param xmlMessages
	 *            the XML messages
	 * @param circleCode
	 *            the circle code
	 * @return the boolean true if batch execution info saved successfully.
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	Boolean saveBatchExecutionInfo(String[] xmlMessages, String circleCode) throws ExecutionFailedException,
			UnknownHostException;
}
