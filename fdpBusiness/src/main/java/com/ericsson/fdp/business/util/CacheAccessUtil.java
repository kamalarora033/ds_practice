package com.ericsson.fdp.business.util;

import java.util.Set;

import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * The Class CacheAccessUtil.
 * 
 * @author Ericsson
 */
public class CacheAccessUtil {

	/**
	 * Instantiates a new cache access util.
	 */
	private CacheAccessUtil() {

	}

	/**
	 * Does icr subscriber exist in cache.
	 * 
	 * @param circleCode
	 *            the circle code
	 * @param msisdn
	 *            the msisdn
	 * @return the boolean
	 */
	@SuppressWarnings("unchecked")
	public static Boolean doesICRSubscriberExistInCache(final String circleCode, final String msisdn) {
		final FDPAppBag appBag = new FDPAppBag(AppCacheSubStore.ICR_SUBSCRIBERS, circleCode);
		Set<String> msisdnNumbers;
		try {
			msisdnNumbers = (Set<String>) ApplicationConfigUtil.getApplicationConfigCache().getValue(appBag);
		} catch (final ExecutionFailedException e) {
			return false;
		}
		return msisdnNumbers != null ? msisdnNumbers.contains(msisdn) : false;
	}
}
