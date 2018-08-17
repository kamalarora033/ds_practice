package com.ericsson.fdp.business.monitoring.cache;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.common.dto.TrapError;
import com.ericsson.fdp.common.enums.trap.TrapErrorCodes;
import com.ericsson.fdp.common.enums.trap.TrapSeverity;
import com.ericsson.fdp.common.exception.FDPException;
import com.ericsson.fdp.common.logging.Event;
import com.ericsson.fdp.core.cache.service.AppCacheInitializerService;
import com.ericsson.fdp.core.dsm.framework.service.CacheLoadState;
import com.ericsson.fdp.core.logging.FDPLoggerFactory;
import com.ericsson.fdp.core.utils.SNMPUtil;

/**
 * The Class AppCacheInitializer.
 */
// @Named("AppCacheInitializer")
public class AppCacheInitializer extends AbstractCacheInitializer {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(AppCacheInitializer.class);

	/** The app cache initializer service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/AppCacheInitializerServiceImpl")
	private AppCacheInitializerService appCacheInitializerService;

	@Override
	public boolean initializeCache() {
		boolean isCacheInitialized = false;
		try {
			isCacheInitialized = appCacheInitializerService.initializeCache();
			FDPLoggerFactory.getGenerateAlarmLogger().warn(
					"",
					new Event(TrapSeverity.CLEAR, new TrapError(
							TrapErrorCodes.FAILED_TO_INITILIZE_APP_CACHE),
							SNMPUtil.getIPAddess()));
			if (isCacheInitialized && next() != null) {
				return next().initializeCache();
			}
		} catch (FDPException e) {
			FDPLoggerFactory.getGenerateAlarmLogger().warn(
					"",
					new Event(TrapSeverity.CRITICAL, new TrapError(
							TrapErrorCodes.FAILED_TO_INITILIZE_APP_CACHE),
							SNMPUtil.getIPAddess()));
			LOGGER.error("Failed to initialize application cache", e);
			isCacheInitialized = false;
		}
		return isCacheInitialized;
	}

	@Override
	public boolean isCacheNeedsLoading() {
		return appCacheInitializerService.isCacheNeedsLoading();
	}

	@Override
	public CacheLoadState setCacheStatus(CacheLoadState newState) {
		return appCacheInitializerService.setCacheStatus(newState);
	}
}
