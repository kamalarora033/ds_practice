package com.ericsson.fdp.business.monitoring.cache;

import com.ericsson.fdp.core.dsm.framework.service.CacheLoadState;

public class RequestCacheInitializer extends AbstractCacheInitializer {

	@Override
	public boolean initializeCache() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCacheNeedsLoading() {
		return false;
	}

	@Override
	public CacheLoadState setCacheStatus(CacheLoadState  newState) {
		return null;
	}

}
