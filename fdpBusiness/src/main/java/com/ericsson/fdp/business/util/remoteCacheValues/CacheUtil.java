package com.ericsson.fdp.business.util.remoteCacheValues;

import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPRequestBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This class is used to get the cache values which have been requested.
 */
@Stateless
public class CacheUtil implements CacheUtility {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(CacheUtil.class);

	/**
	 * Instantiates a new cache util.
	 */

	@Override
	public String getCacheValue(final Integer cacheOption, final String circleCode, final String substore,
			final String key) throws ExecutionFailedException {
		String outputValue = null;
		// The key are checked for string, integer and long values.
		if (cacheOption == 1) {
			final FDPCache<FDPMetaBag, FDPCacheable> metaCache = ApplicationConfigUtil.getMetaDataCache();
			Object value = getValueFromMetaCache(circleCode, substore, key, metaCache);
			if (value == null) {
				try {
					final Integer intKey = Integer.parseInt(key);
					value = getValueFromMetaCache(circleCode, substore, intKey, metaCache);
				} catch (final NumberFormatException e) {
					LOGGER.error("Exception Occured.", e);
					// Do nothing
				}
			}
			if (value == null) {
				try {
					final Long longKey = Long.parseLong(key);
					value = getValueFromMetaCache(circleCode, substore, longKey, metaCache);
				} catch (final NumberFormatException e) {
					LOGGER.error("Exception Occured.", e);
					// Do nothing
				}
			}
			if (value != null) {
				outputValue = value.toString();
			}
		}
		if (cacheOption == 2) {
			final FDPCache<FDPAppBag, Object> appCache = ApplicationConfigUtil.getApplicationConfigCache();
			Object value = getValueFromAppCache(substore, key, appCache);
			if (value == null) {
				try {
					final Integer intKey = Integer.parseInt(key);
					value = getValueFromAppCache(substore, intKey, appCache);
				} catch (final NumberFormatException e) {
					LOGGER.error("Exception Occured.", e);
					// Do nothing
				}
			}
			if (value == null) {
				try {
					final Long longKey = Long.parseLong(key);
					value = getValueFromAppCache(substore, longKey, appCache);
				} catch (final NumberFormatException e) {
					LOGGER.error("Exception Occured.", e);
					// Do nothing
				}
			}
			if (value != null) {
				outputValue = value.toString();
			}
		}
		if (cacheOption == 3) {
			FDPCache<FDPRequestBag, FDPCacheable> requestCache = null;
			if (ChannelType.USSD.equals(ChannelType.valueOf(substore))) {
				requestCache = ApplicationConfigUtil.getRequestCacheForUSSD();
			} else {
				requestCache = ApplicationConfigUtil.getRequestCacheForSMS();
			}
			final Object value = getValueFromRequestCache(key, requestCache);
			if (value != null) {
				outputValue = value.toString();
			}
		}
		return outputValue;
	}

	private Object getValueFromRequestCache(final String key, final FDPCache<FDPRequestBag, FDPCacheable> requestCache) {
		return requestCache.getValue(new FDPRequestBag(key));
	}

	private Object getValueFromAppCache(final String substore, final Object key,
			final FDPCache<FDPAppBag, Object> appCache) {
		return appCache.getValue(new FDPAppBag(AppCacheSubStore.valueOf(substore), key));
	}

	private FDPCacheable getValueFromMetaCache(final String circleCode, final String substore, final Object key,
			final FDPCache<FDPMetaBag, FDPCacheable> metaCache) {
		return metaCache.getValue(new FDPMetaBag(new FDPCircle(-1L, circleCode, null), ModuleType.valueOf(substore),
				key));
	}

}
