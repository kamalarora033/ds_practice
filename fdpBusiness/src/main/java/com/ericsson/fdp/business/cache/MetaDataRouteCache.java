package com.ericsson.fdp.business.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.Singleton;
import com.ericsson.fdp.FDPCacheable;

/**
 * The Class DataCache is capable of storing Map in key-value pair.
 * 
 * @author Ericsson
 */
//@Singleton
public class MetaDataRouteCache implements FDPCacheable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -247115439520038531L;

	/** The meta data map. */
	private Map<Object, Object> metaDataMap;

	/** The data cache. */
	private static MetaDataRouteCache dataCache = new MetaDataRouteCache();

	/**
	 * Gets the meta data map.
	 * 
	 * @return the metaDataMap
	 */
	public Map<Object, Object> getmetaDataMap() {
		return metaDataMap;
	}

	/**
	 * Setmeta data map.
	 * 
	 * @param metaDataMapToSet the metaDataMap to set
	 */
	public void setmetaDataMap(final Map<Object, Object> metaDataMapToSet) {
		this.metaDataMap = metaDataMapToSet;
	}

	/**
	 * Private constructor to prevent object construction.
	 */
	public MetaDataRouteCache() {
		metaDataMap = new ConcurrentHashMap<Object, Object>();
	}

	/**
	 * This method is used to get the DSM service instance.
	 * 
	 * @return DSM service instance.
	 */
	public static MetaDataRouteCache getInstance() {
		return dataCache;
	}

	/**
	 * Gets the value from map.
	 * 
	 * @param key the key
	 * @return the value
	 */
	public Object getValue(final Object key) {
		return metaDataMap.get(key);
	}

	/**
	 * Put value in Map.
	 * 
	 * @param key represents key.
	 * @param value represents need to be store.
	 */
	public void putValue(final Object key, final Object value) {
		metaDataMap.put(key, value);
	}

	/**
	 * Checks Key in the Map if exists then returns true otherwise false.
	 * 
	 * @param key represents need to be search in map.
	 * @return represents if key exists in map then returns true otherwise false.
	 */
	public boolean isKeyExist(final String key) {
		return metaDataMap.containsKey(key);
	}

}
