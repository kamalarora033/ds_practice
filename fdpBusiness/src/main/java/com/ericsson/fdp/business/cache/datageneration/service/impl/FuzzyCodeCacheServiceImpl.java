package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.FuzzyCodeCacheService;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.vo.FDPFuzzyCodeVo;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.FDPFuzzyCodeDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.entity.FDPFuzzyCode;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;

@Stateless(mappedName = "FuzzyCodeCacheService")
public class FuzzyCodeCacheServiceImpl implements FuzzyCodeCacheService {
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(OfferAttributeDataServiceImpl.class);

	private static final Mapper MAPPER = new DozerBeanMapper();

	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		// spId in case of OTHERS and productId in Product
		Long id = null;
		if (updateCacheDTO != null) {
			id = updateCacheDTO.getId();
			if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
				return fdpCache.removeKey(new FDPMetaBag(updateCacheDTO
						.getCircle(), ModuleType.FUZZY_CODES, updateCacheDTO
						.getId()));

			}
		}
		try {
			updateCache(id, updateCacheDTO.getCircle());
		} catch (ExpressionFailedException e) {
			/*System.out.println("Failed in loading Cache "
					+ ModuleType.FUZZY_CODES + updateCacheDTO.getId());*/
			LOGGER.info("Failed in loading Cache " + ModuleType.FUZZY_CODES + updateCacheDTO.getId());
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {
		try {
			this.updateCache(null, fdpCircle);
		} catch (ExpressionFailedException e) {
			throw new FDPServiceException(e);
		}

		return true;

	}

	@SuppressWarnings("unused")
	private void updateCache(Long id, FDPCircle fdpCircle)
			throws ExpressionFailedException {
		 List<FDPFuzzyCodeDTO> fdpFuzzyCodeDTOLst = new ArrayList<FDPFuzzyCodeDTO>();
		
		List<FDPCircle> fdpActiveCircleList=entityService.getActiveCircleVOs();
		for (Iterator<FDPCircle> iterator = fdpActiveCircleList.iterator(); iterator.hasNext();) {
			FDPCircle fdpCircletemp=(FDPCircle) iterator.next();
			List<FDPFuzzyCodeDTO> fdpFuzzyCode=entityService.getFuzzyCode(fdpCircletemp.getCircleName());
			fdpFuzzyCodeDTOLst.addAll(fdpFuzzyCode);
			
		}
	
	initializeUpdateFuzzyCode(fdpFuzzyCodeDTOLst,fdpCircle);

	}
	
	private void initializeUpdateFuzzyCode(List<FDPFuzzyCodeDTO> fdpFuzzyCodeDTOLst,FDPCircle fdpCircle)
	{
		FDPMetaBag metaBag ;
		for (Iterator<FDPFuzzyCodeDTO> iterator = fdpFuzzyCodeDTOLst.iterator(); iterator.hasNext();) {
			FDPFuzzyCodeDTO fdpFuzzyCodeDTO = (FDPFuzzyCodeDTO) iterator.next();
			metaBag= new FDPMetaBag(fdpCircle, ModuleType.FUZZY_CODES, fdpFuzzyCodeDTO.getCommand_display_name()+FDPConstant.UNDERSCORE+fdpFuzzyCodeDTO.getFdp_fuzzyCode());
			fdpCache.putValue(metaBag, MAPPER.map(fdpFuzzyCodeDTO,FDPFuzzyCodeVo.class));
		}
		
	}
	
	

	@Override
	public ModuleType getModuleType() {
		// TODO Auto-generated method stub
		return ModuleType.FUZZY_CODES;
	}

}
