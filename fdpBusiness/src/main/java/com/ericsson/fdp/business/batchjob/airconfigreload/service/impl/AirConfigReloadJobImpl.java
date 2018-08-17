package com.ericsson.fdp.business.batchjob.airconfigreload.service.impl;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.batchjob.airconfigreload.service.AirConfigReloadJob;
import com.ericsson.fdp.business.cache.datageneration.service.AirConfigCacheService;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.core.entity.service.EntityService;

@Stateless(name = "AirConfigReloadJobImpl")
@Startup
public class AirConfigReloadJobImpl implements AirConfigReloadJob {

	@Inject
	private AirConfigCacheService airconfigcache;

	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	private static final Logger LOGGER = LoggerFactory.getLogger(AirConfigReloadJob.class);
	
	@Override
	public void execute() {
		try {
			LOGGER.debug("Initializing meta cache for air configurations");
			airconfigcache.initializeMetaCache(null);
		} catch (FDPServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
