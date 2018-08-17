package com.ericsson.fdp.business.cache;

import java.util.Map;

import javax.ejb.DependsOn;
import javax.ejb.Stateless;

import org.infinispan.client.hotrod.RemoteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.core.dsm.framework.FDPCacheBuilder;
import com.ericsson.fdp.core.dsm.framework.FDPCacheType;
import com.ericsson.fdp.core.dsm.framework.service.CacheLoadState;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;

/**
 * The MetaDataCache is responsible for storing metadata of FDP commands and
 * parameters.
 * 
 * @author Ericsson
 */
@Stateless(name = "MetaDataCache")
@DependsOn(value = "FDPCacheBuilder")
public class MetaDataCache implements FDPCache<FDPMetaBag, FDPCacheable> {
	
	/** The key for Cache State */
	private static final String CACHE_STATE_KEY = MetaDataCache.class.getName()+".status";

	/**
	 * The Constant metaDataCache. MetaDataCache Structure - A Map whose key is
	 * combination of circleCode and module. Internal structure of Module Map -
	 * Map whose key is Module Name and value is map of different module Ids.
	 * Internal structure of Module Map Id - Map whose key is Module ID and
	 * value is FDPCacheable object that needs to be stored in Cache.
	 **/
	//private static final Cache<Object, Object> METADATA_CACHE = (Cache<Object, Object>) FDPCacheBuilder.getCache(FDPCacheType.META_CACHE);
	
	private static final RemoteCache<Object, Object> METADATA_CACHE = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE);
	
	private static final RemoteCache<Object, Object> SP_PRODUCT = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.SP_PRODUCT);
	
	private static final RemoteCache<Object, Object> SP_OTHERS = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.SP_OTHERS);
	
	private static final RemoteCache<Object, Object> DM = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.DM);
	
	private static final RemoteCache<Object, Object> PRODUCT = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.PRODUCT);
	
	private static final RemoteCache<Object, Object> PRODUCT_NAME_ID_MAP = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.PRODUCT_NAME_ID_MAP);
	
	private static final RemoteCache<Object, Object> COMMAND = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.COMMAND);
	
	private static final RemoteCache<Object, Object> AIR_RECHARGE = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.AIR_RECHARGE);
	
	private static final RemoteCache<Object, Object> NOTIFICATION = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.NOTIFICATION);
	
	private static final RemoteCache<Object, Object> POLICY = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.POLICY);
	
	private static final RemoteCache<Object, Object> TARIFF_ENQUIRY_ATTRIBUTES = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.TARIFF_ENQUIRY_ATTRIBUTES);
	
	private static final RemoteCache<Object, Object> DYNAMIC_MENU_CODE_ALIAS = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.DYNAMIC_MENU_CODE_ALIAS);
	
	private static final RemoteCache<Object, Object> UNIT_DISPLAY_FORMAT = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.UNIT_DISPLAY_FORMAT);
	
	private static final RemoteCache<Object, Object> FAULT_CODE_NOTIFICATION_MAPPING = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.FAULT_CODE_NOTIFICATION_MAPPING);

	private static final RemoteCache<Object, Object> RESPONSE_CODE_NOTIFICATION_MAPPING = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.RESPONSE_CODE_NOTIFICATION_MAPPING);
	
	private static final RemoteCache<Object, Object> PRODUCT_ALIAS = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.PRODUCT_ALIAS);
	
	private static final RemoteCache<Object, Object> TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION);
	
	private static final RemoteCache<Object, Object> PRODUCT_CATEGORY = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.PRODUCT_CATEGORY);
	
	private static final RemoteCache<Object, Object> PRODUCT_COUPON_CODE = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.PRODUCT_COUPON_CODE);
	
	private static final RemoteCache<Object, Object> PRODUCT_COUPON_MAP = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.PRODUCT_COUPON_MAP);
	
	private static final RemoteCache<Object, Object> PRODUCT_ATTRIBUTE_MAP=(RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE, ModuleType.PRODUCT_ATTRIBUTE_MAP);
	
	private static final RemoteCache<Object, Object> HANDSET_4G_MAP = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE, ModuleType.HANDSET_4G);
			
	
	/*private static final RemoteCache<Object, Object> OFFER_ATTRIBUTE_MAP = (RemoteCache<Object, Object>) FDPCacheBuilder
			.getCache(FDPCacheType.META_CACHE,ModuleType.OFFER_ATTRIBUTE);*/
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(MetaDataCache.class);

	/** The Constant KEY_SEPARATOR. */
	private static final String KEY_SEPARATOR = "_";

	@Override
	public FDPCacheable getValue(final FDPMetaBag fdpMetaBagKey) {
		checkArguments(fdpMetaBagKey);
		//Object value = METADATA_CACHE.get(getFullMetaKey(fdpMetaBagKey));
		final RemoteCache<Object, Object> remoteCache = getCacheStore(fdpMetaBagKey.getModuleType());
		Object value = remoteCache.get(getFullMetaKey(fdpMetaBagKey));
		//System.out.println("Getting remoteCache:"+remoteCache.getName()+", key:"+fdpMetaBagKey+", value:"+value);
		//return CommandCacheUtil.cloneObject(value != null && value instanceof FDPCacheable ? (FDPCacheable) value : null);
		return value != null && value instanceof FDPCacheable ? (FDPCacheable) value : null;
	}

	private String getFullMetaKey(FDPMetaBag fdpMetaBagKey) {
		return new StringBuilder(getCircleModuleKey(fdpMetaBagKey)).append(KEY_SEPARATOR).append(fdpMetaBagKey.getModuleId()).toString();
	}

	@Override
	public void putValue(final FDPMetaBag fdpMetaBagKey, final FDPCacheable value) {
		checkArguments(fdpMetaBagKey);
		//METADATA_CACHE.put(getFullMetaKey(fdpMetaBagKey), value);
		final RemoteCache<Object, Object> remoteCache = getCacheStore(fdpMetaBagKey.getModuleType());
		remoteCache.put(getFullMetaKey(fdpMetaBagKey), value);
		//System.out.println("Putting remoteCache:"+remoteCache.getName()+", key:"+fdpMetaBagKey+", value:"+value);
//		printWholeCacheStore(remoteCache, fdpMetaBagKey.getModuleType());
		LOGGER.debug("Value of MetaBagKey is {}", fdpMetaBagKey);
		LOGGER.debug("Key put in module ID cache is {} and value is {}", fdpMetaBagKey.getModuleId(), value);
	}

	@Override
	public boolean removeKey(final FDPMetaBag fdpMetaBagKey) {
		checkArguments(fdpMetaBagKey);
		LOGGER.debug("In removeKey {}", getFullMetaKey(fdpMetaBagKey));
		//return METADATA_CACHE.remove(getFullMetaKey(fdpMetaBagKey)) != null;
		return getCacheStore(fdpMetaBagKey.getModuleType()).remove(getFullMetaKey(fdpMetaBagKey)) != null;
	}

	@Override
	public boolean removeSubStore(final FDPMetaBag fdpMetaBagKey) {
		checkArguments(fdpMetaBagKey);
		final String circleModuleKey = getCircleModuleKey(fdpMetaBagKey);
		LOGGER.debug("In removeSubStore function of MetaCache CircleModule key is : {}", circleModuleKey);
		//AtomicMapLookup.removeAtomicMap(METADATA_CACHE, circleModuleKey);
		
		LOGGER.debug("Removed circleModule {} from MetaCache", circleModuleKey);
		return true;
	}

	/**
	 * Gets the circle module key.
	 * 
	 * @param fdpMetaBagKey
	 *            the fdp meta bag key
	 * @return the circle module key
	 */
	private String getCircleModuleKey(final FDPMetaBag fdpMetaBagKey) {
		return new StringBuilder(fdpMetaBagKey.getFdpCircle().getCircleCode()).append(KEY_SEPARATOR)
				.append(fdpMetaBagKey.getModuleType().getName()).toString();
	}

	/**
	 * Check arguments.
	 * 
	 * @param fdpMetaBagKey
	 *            the fdp meta bag key
	 */
	private void checkArguments(final FDPMetaBag fdpMetaBagKey) {
		if (fdpMetaBagKey == null || fdpMetaBagKey.getFdpCircle() == null) {
			throw new IllegalArgumentException("FdpMetaBag or FDPCircle can't be null");
		}
	}

	@Override
	public Boolean isCacheNeedsLoading() {
		
		CacheLoadState cacheStatus = (CacheLoadState)METADATA_CACHE.get(CACHE_STATE_KEY);
		
		if(CacheLoadState.INITIALIZED.equals(cacheStatus)
				|| CacheLoadState.INITIALIZING.equals(cacheStatus)){
			return Boolean.FALSE;
		} else{
			return Boolean.TRUE;
		}
		
		/*boolean result = true;
		try {
			final EntityService entityService = (EntityService) ApplicationConfigUtil
					.getBean("java:global/fdpBusiness-ear/fdpCoreServices-1.0/EntityServiceImpl");
			final List<FDPCircleDTO> circles = entityService.getAllCircle();
			final String key = circles.get(0).getCircleCode();
			System.out.println("Looking for circle :"+key);
			System.out.println(METADATA_CACHE.get(key));
			if (circles != null && !circles.isEmpty() && METADATA_CACHE.get(circles.get(0).getCircleCode()) != null) {
				result = false;
			}
		} catch (final NamingException e) {
			e.printStackTrace();
			result = true;
		}
		try {
			int sp_product_cache_size = SP_PRODUCT.entrySet().size();
			System.out.println("isCacheNeedsLoading:"+sp_product_cache_size);
			if(sp_product_cache_size > 0) {
				result = false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = true;
		}
		return result;*/
	}
	
	/**
	 * This method will get the stores for each meta cache.
	 * 
	 * @param moduleType
	 * @return
	 */
	private static RemoteCache<Object, Object> getCacheStore(final ModuleType moduleType) {
		RemoteCache<Object, Object> remoteCache = null;
		switch (moduleType) {
		case SP_PRODUCT:
			remoteCache = SP_PRODUCT;
			break;
		case SP_OTHERS:
			remoteCache = SP_OTHERS;
			break;
		case DM:
			remoteCache = DM;
			break;
		case PRODUCT:
			remoteCache = PRODUCT;
			break;
		case PRODUCT_NAME_ID_MAP:
			remoteCache = PRODUCT_NAME_ID_MAP;
			break;
		case COMMAND:
			remoteCache = COMMAND;
			break;
		case AIR_RECHARGE:
			remoteCache = AIR_RECHARGE;
			break;
		case NOTIFICATION:
			remoteCache = NOTIFICATION;
			break;
		case POLICY:
			remoteCache = POLICY;
			break;
		case TARIFF_ENQUIRY_ATTRIBUTES:
			remoteCache = TARIFF_ENQUIRY_ATTRIBUTES;
			break;
		case DYNAMIC_MENU_CODE_ALIAS:
			remoteCache = DYNAMIC_MENU_CODE_ALIAS;
			break;
		case UNIT_DISPLAY_FORMAT:
			remoteCache = UNIT_DISPLAY_FORMAT;
			break;
		case FAULT_CODE_NOTIFICATION_MAPPING:
			remoteCache = FAULT_CODE_NOTIFICATION_MAPPING;
			break;
		case RESPONSE_CODE_NOTIFICATION_MAPPING:
			remoteCache = RESPONSE_CODE_NOTIFICATION_MAPPING;
			break;
		case PRODUCT_ALIAS:
			remoteCache = PRODUCT_ALIAS;
			break;
		case TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION:
			remoteCache = TARIFF_ENQUIRY_ATTRIBUTE_EXPRESSION;
			break;
		case PRODUCT_CATEGORY:
			remoteCache = PRODUCT_CATEGORY;
			break;
		case PRODUCT_COUPON_CODE:
			remoteCache = PRODUCT_COUPON_CODE;
			break;
		case PRODUCT_COUPON_MAP:
			remoteCache = PRODUCT_COUPON_MAP;
			break;
		case PRODUCT_ATTRIBUTE_MAP:
			remoteCache=PRODUCT_ATTRIBUTE_MAP;
			break;
		case HANDSET_4G:
			remoteCache = HANDSET_4G_MAP;
			break;
		default:
			remoteCache = METADATA_CACHE;
			break;
		}
		remoteCache = (null == remoteCache ? METADATA_CACHE : remoteCache) ;
		//System.out.println("getCacheStore moduleType:"+moduleType+" ,remoteCache:"+remoteCache);
		return remoteCache;
	}
	
	/*private void printWholeCacheStore(final RemoteCache<Object, Object> remoteCache, final ModuleType moduleType) {
		for(Entry<Object, Object> entry : remoteCache.entrySet()) {
			System.out.println("########## ModuleType:"+moduleType+", key:"+entry.getKey()+", value:"+entry.getValue());
		}
		
		for(final Object object : remoteCache.keySet()) {
			//System.out.println("################ ModuleType:"+moduleType+" , key:"+object);
		}
	}*/

	@Override
	public CacheLoadState setCacheStatus(CacheLoadState newStatus) {
		CacheLoadState oldState = (CacheLoadState)METADATA_CACHE.get(CACHE_STATE_KEY);
		METADATA_CACHE.put(CACHE_STATE_KEY, newStatus);
		return oldState;
	}
	
	public static Map<Object,Object> getAllValue(final ModuleType moduleType) {
		Map<Object,Object> productList = null;
        final RemoteCache<Object, Object> remoteCache = getCacheStore(moduleType);
        productList = remoteCache.getBulk();
        return productList;
	}

}
