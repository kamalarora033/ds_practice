package com.ericsson.fdp.business.cache.datageneration.service;

import com.ericsson.fdp.business.cache.MetaDataService;
/**
 * 
 * @author Ericsson
 *
 */
public interface SPDataService extends MetaDataService {
	
	/**
	 * This method is for quick fix for issue in DeletePAM command.
	 * This method should not be removed till a proper fix for the same can be applied.
	 * Please discuss the change with relevant person if modification for the same is done.
	 */
	public void refreshDataCache();

}
