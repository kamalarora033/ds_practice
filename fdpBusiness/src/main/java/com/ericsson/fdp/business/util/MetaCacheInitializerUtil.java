package com.ericsson.fdp.business.util;

import java.util.List;

import com.ericsson.fdp.business.cache.MetaDataService;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;

/**
 * This class is used to provide utility methods for class conversions.
 */
public final class MetaCacheInitializerUtil {

	/**
	 * 
	 * @param metaDataService
	 * @param moduleType
	 * @param circles
	 * @throws FDPServiceException
	 */
	public static void initializeCacheForModule(final MetaDataService metaDataService, final ModuleType moduleType, final List<FDPCircle> circles)
			throws FDPServiceException {
		for (FDPCircle circle : circles) {
			Long start = System.currentTimeMillis();
			metaDataService.initializeMetaCache(circle);
			System.out.println("\n Initialized " + moduleType.getName() + " cache for circle " + circle.getCircleName()
					+ " in: " + (System.currentTimeMillis() - start));
		}
	}


}
