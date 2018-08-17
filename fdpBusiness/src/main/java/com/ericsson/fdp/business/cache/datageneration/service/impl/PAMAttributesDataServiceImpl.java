package com.ericsson.fdp.business.cache.datageneration.service.impl;
/**
 * This is the implementation class of PAM business interface
 */
import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.PAMAttributesDataService;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;

@Stateless(mappedName = "pamAttributesDataService")
public class PAMAttributesDataServiceImpl implements PAMAttributesDataService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PAMAttributesDataServiceImpl.class);
	
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;
	
	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		// TODO Auto-generated method stub
		Long id = null;
		if (updateCacheDTO != null) {
			id = updateCacheDTO.getId();
			if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
						return fdpCache.removeKey(new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.PAM_OFFER_ATTRIBUTE,
								updateCacheDTO.getId()));
						
			}
		}
		try {
			updateCache(id, updateCacheDTO.getCircle());
		} catch (ExpressionFailedException e) {
			/*System.out.println("Failed in loading Cache "+ ModuleType.PAM_OFFER_ATTRIBUTE+
								updateCacheDTO.getId());*/
			LOGGER.info("Failed in loading Cache "+ ModuleType.PAM_OFFER_ATTRIBUTE + updateCacheDTO.getId());
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {
		try {
			this.updateCache(null, fdpCircle);
		} catch (ExpressionFailedException e) {
			LOGGER.error(e.getMessage());
			throw new FDPServiceException(e);
		}
	
	return true;
	}

	
	/**
	 * 
	 * @param id
	 * @param fdpCircle
	 * @throws ExpressionFailedException
	 */
	private void updateCache(Long id , FDPCircle fdpCircle) throws ExpressionFailedException
	{
		if(id!=null)
		{
			
		}
	}
	
	
	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return ModuleType.PAM_OFFER_ATTRIBUTE;
	}

}
