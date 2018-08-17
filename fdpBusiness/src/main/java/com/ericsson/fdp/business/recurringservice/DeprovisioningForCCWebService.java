package com.ericsson.fdp.business.recurringservice;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.core.exception.ExecutionFailedException;

@Remote
public interface DeprovisioningForCCWebService {
	/**
	 * This method is used for deprovisioning of all products with given ids.
	 * 
	 * @param productIds
	 * @return
	 * @throws ExecutionFailedException
	 * 
	 */
	String executeDeprovisioning(final List<Long> productIds, final String msisdn) throws ExecutionFailedException;
}
