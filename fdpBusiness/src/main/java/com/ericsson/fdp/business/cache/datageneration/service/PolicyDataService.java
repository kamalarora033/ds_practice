package com.ericsson.fdp.business.cache.datageneration.service;

import javax.ejb.Remote;

import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.common.exception.FDPServiceException;

/**
 * The Interface PolicyDataService.
 *
 * @author Ericsson
 */
@Remote
public interface PolicyDataService {

	/**
	 * Initialize The Policy Cache.
	 *
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws ExpressionFailedException
	 * @throws Exception
	 *
	 */
	public void initializeUpdatePolicyCache() throws FDPServiceException;
}
