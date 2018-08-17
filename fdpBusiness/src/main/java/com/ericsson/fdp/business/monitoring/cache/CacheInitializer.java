package com.ericsson.fdp.business.monitoring.cache;

import com.ericsson.fdp.core.dsm.framework.service.CacheLoadState;

/**
 * The Interface CacheInitializer.
 */
public interface CacheInitializer {
	
	/**
	 * Initialize cache.
	 *
	 * @return true, if successful
	 */
	boolean initializeCache();
	
	/**
	 * Next.
	 *
	 * @return the cache initializer
	 */
	CacheInitializer next();
	
	/**
	 * Checks if is cache needs loading.
	 *
	 * @return true, if is cache needs loading
	 */
	boolean isCacheNeedsLoading();
	
	/**
	 * This method updates the cache state.
	 * @param newState
	 * @return Old Cache State
	 */
	CacheLoadState setCacheStatus(CacheLoadState newState);

}
