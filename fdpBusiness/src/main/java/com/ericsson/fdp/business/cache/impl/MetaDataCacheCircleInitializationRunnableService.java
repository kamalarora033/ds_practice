package com.ericsson.fdp.business.cache.impl;

import com.ericsson.fdp.business.cache.MetaDataService;
import com.ericsson.fdp.business.cache.RunnableService;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;

/**
 * The Interface MetaDataService.
 */
public final class MetaDataCacheCircleInitializationRunnableService implements RunnableService {

	private MetaDataService metaDataService;

	private FDPCircle fdpCircle;

	public MetaDataCacheCircleInitializationRunnableService(final MetaDataService metaDataService, final FDPCircle fdpCircle) {
		this.metaDataService = metaDataService;
		this.fdpCircle = fdpCircle;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("Thread-" + metaDataService.getModuleType().getName() + "-" + fdpCircle.getCircleName());
		try {
			this.metaDataService.initializeMetaCache(fdpCircle);
		} catch (FDPServiceException e) {
			throw new RuntimeException("Could not initialize " + metaDataService.getClass(), e);
		}
	}

}
