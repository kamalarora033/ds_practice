package com.ericsson.fdp.business.menu;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.FDPSMPPRequest;

/**
 * This interface provides the method to execute dynamic menu.
 * 
 * @author Ericsson
 * 
 */
public interface FDPDynamicMenu {

	/**
	 * This method is used to execute the dynamic menu.
	 * 
	 * @param dynamicMenuRequest
	 *            The request for dynamic menu.
	 * @return The response of dynamic menu.
	 * @throws ExecutionFailedException
	 *             Exception, if execution fails.
	 */
	FDPResponse executeDynamicMenu(FDPSMPPRequest dynamicMenuRequest) throws ExecutionFailedException;

}
