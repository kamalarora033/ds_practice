package com.ericsson.fdp.business.cache.impl;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ericsson.fdp.business.cache.MetaDataService;
import com.ericsson.fdp.business.cache.RunnableService;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;

/**
 * The Interface MetaDataService.
 */
public final class MetaDataCacheInitializationRunnableService implements RunnableService {

	private List<FDPCircle> fdpCircles;
	private MetaDataService metaDataService;
	private ModuleType moduleType;

	public MetaDataCacheInitializationRunnableService(final MetaDataService metaDataService,
			final List<FDPCircle> fdpCircles, final ModuleType moduleType) {
		this.metaDataService = metaDataService;
		this.fdpCircles = fdpCircles;
		this.moduleType = moduleType;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("Thread-Module-" + moduleType.getName());
		long startTime = System.currentTimeMillis();
		ExecutorService executorService = Executors.newFixedThreadPool(fdpCircles.size());
		for (FDPCircle circle : fdpCircles) {
			Long start = System.currentTimeMillis();
			executorService.execute(new MetaDataCacheCircleInitializationRunnableService(metaDataService, circle));
			System.out.println("\n Initialized " + moduleType.getName() + " cache for circle " + circle.getCircleName()
					+ " in: " + (System.currentTimeMillis() - start));
		}
		executorService.shutdown();
		while (!executorService.isTerminated()) {
			// Wait for the executors to terminate.
		}
		System.out.println(moduleType.name() + " Time taken is "
				+ (System.currentTimeMillis() - startTime));
	}

}
