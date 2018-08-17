package com.ericsson.fdp.business.monitoring.cache;

/**
 * The Class AbstractCacheInitializer.
 */
public abstract class AbstractCacheInitializer implements CacheInitializer {

	/** The next cache initializer. */
	CacheInitializer nextCacheInitializer;	

	/**
	 * Sets the next.
	 *
	 * @param nextCacheInitializer the new next
	 */
	public void setNext(CacheInitializer nextCacheInitializer) {
		this.nextCacheInitializer = nextCacheInitializer;
	}
	
	
	@Override
	public CacheInitializer next() {
		return nextCacheInitializer;
	}

}
