package com.ericsson.fdp.business.util.remoteCacheValues;

import javax.ejb.Remote;

import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * This interface exposes the utility for accessing the cache values using
 * external application..
 * 
 * @author Ericsson
 * 
 */
@Remote
public interface CacheUtility {
	/**
	 * This method returns the cache value for the provided options.
	 * 
	 * @param cacheOption
	 *            the cache to be used, 1-meta cache, 2-application cache,
	 *            3-request cache.
	 * @param circleCode
	 *            the circle code to be used.
	 * @param substore
	 *            the substore to be used.
	 * @param key
	 *            the key to be used.
	 * @return the string value of the object in the cache.
	 * @throws ExecutionFailedException
	 *             Exception if any.
	 */
	public String getCacheValue(Integer cacheOption, String circleCode, String substore, String key)
			throws ExecutionFailedException;
}
