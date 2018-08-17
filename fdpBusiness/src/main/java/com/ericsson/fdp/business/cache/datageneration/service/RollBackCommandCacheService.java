package com.ericsson.fdp.business.cache.datageneration.service;

import com.ericsson.fdp.common.exception.FDPServiceException;

public interface RollBackCommandCacheService {

	boolean initializeRollBackCommandCache() throws FDPServiceException;

}
