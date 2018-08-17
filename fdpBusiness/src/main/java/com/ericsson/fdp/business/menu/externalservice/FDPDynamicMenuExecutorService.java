package com.ericsson.fdp.business.menu.externalservice;

import javax.ejb.Remote;

import com.ericsson.fdp.business.menu.FDPDynamicMenu;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

/**
 * This interface is used for executing dynamic menu service from external
 * systems.
 * 
 * @author Ericsson
 * 
 */
@Remote
public interface FDPDynamicMenuExecutorService {

	/**
	 * This method is used to execute the dynamic menu.
	 * 
	 * @param input
	 *            the user input.
	 * @param msisdn
	 *            the msisdn.
	 * @param channel
	 *            the channel.
	 * @return the response of execution.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	FDPResponse executeDynamicMenu(FDPSMPPRequestImpl fdpsmppRequestImpl, FDPDynamicMenu dynamicMenu) throws ExecutionFailedException;

}
