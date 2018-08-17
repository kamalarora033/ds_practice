package com.ericsson.fdp.business;

import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The class implementing this interface can be roll backed.
 * 
 * @author Ericsson
 * 
 */
public interface FDPRollbackable {

	/**
	 * The method roll backs the step.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @return True, if the roll back was successful, false otherwise.
	 * @throws RollbackException
	 *             Exception, if any while roll back.
	 */
	boolean performRollback(FDPRequest fdpRequest) throws RollbackException;

}
