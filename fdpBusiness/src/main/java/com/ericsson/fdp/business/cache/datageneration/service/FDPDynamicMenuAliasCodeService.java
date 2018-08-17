package com.ericsson.fdp.business.cache.datageneration.service;

import javax.ejb.Remote;

import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;

/**
 * The Interface FDPDynamicMenuAliasCodeService.
 */
@Remote
public interface FDPDynamicMenuAliasCodeService {

	
	/**
	 * Initialize product alias code cache.
	 * @throws ExecutionFailedException 
	 */
	public void initializeDynamicMenuAliasCodeCache(UpdateCacheDTO updateCacheDTO);
}
