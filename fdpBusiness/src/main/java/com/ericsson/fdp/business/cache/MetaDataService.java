package com.ericsson.fdp.business.cache;

import javax.ejb.Remote;

import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;

/**
 * The Interface MetaDataService.
 */
@Remote
public interface MetaDataService {


	/**
	 * Update meta cache.
	 *
	 * @param updateCacheDTO the update cache dto
	 * @return true, if successful
	 * @throws FDPServiceException the FDP service exception
	 */
	boolean updateMetaCache(UpdateCacheDTO updateCacheDTO) throws FDPServiceException;

	/**
	 * Initialize meta cache.
	 *
	 * @param fdpCircle the fdp circle
	 * @return true, if successful
	 * @throws FDPServiceException the FDP service exception
	 */
	boolean initializeMetaCache(FDPCircle fdpCircle) throws FDPServiceException;

	/**
	 * Gets the module type.
	 *
	 * @return the module type
	 */
	ModuleType getModuleType();

}
