package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.ProductCategoryExpressionService;
import com.ericsson.fdp.business.vo.ProductCategoryCacheDTO;
import com.ericsson.fdp.business.vo.ProductCouponMapCacheDTO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.product.FDPProductCategoryDTO;
import com.ericsson.fdp.dao.dto.product.FDPProductCategoryMappingDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.dao.exception.FDPConcurrencyException;
import com.ericsson.fdp.dao.fdpadmin.FDPProductCategoryDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPProductCategoryMappingDAO;

@Stateless(mappedName = "productCategoryExpressionService")
public class ProductCategoryExpressionServiceImpl implements ProductCategoryExpressionService{

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductDataServiceImpl.class);

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	@Inject
	FDPProductCategoryMappingDAO fdpProductCategoryMappingDAO;

	@Inject
	FDPProductCategoryDAO fdpProductCategoryDAO;

	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		try {
			Long id;
			if (updateCacheDTO != null) {
				id = updateCacheDTO.getId();
				if (ActionTypeEnum.ADD_UPDATE.equals(updateCacheDTO.getAction())) {
					if(ModuleType.PRODUCT_CATEGORY.equals(updateCacheDTO.getModuleType())){
						List<FDPProductCategoryMappingDTO> mappingDTOs = new ArrayList<FDPProductCategoryMappingDTO>();
						List<FDPProductCategoryDTO> categoryDTOs = fdpProductCategoryDAO.getProductCategoryById(id);
						Map<Long, List<String>> productCouponMap = fdpProductCategoryMappingDAO.getAllMappings();
						FDPMetaBag metaBag = new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.PRODUCT_CATEGORY, null);
						FDPMetaBag metaBagMap = new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.PRODUCT_COUPON_MAP, null);
						for(FDPProductCategoryDTO fdpProductCategoryDTO : categoryDTOs){
							mappingDTOs = fdpProductCategoryMappingDAO.getMappings(fdpProductCategoryDTO.getProductCategoryId());
							List<String> productIdStr = new ArrayList<String>();
							for(FDPProductCategoryMappingDTO categoryMappingDTO : mappingDTOs){
								productIdStr.add(String.valueOf(categoryMappingDTO.getProductId().getProductId()));
							}
							fdpProductCategoryDTO.setProductIds(productIdStr);
							if(fdpCache.getValue(metaBag) != null){
								ProductCategoryCacheDTO productCategory = (ProductCategoryCacheDTO) fdpCache.getValue(metaBag);
								productCategory.getProductCategoryMap().put(fdpProductCategoryDTO.getProductCategoryName(), fdpProductCategoryDTO);
								fdpCache.putValue(metaBag, productCategory);
							}else{
								ProductCategoryCacheDTO productCategory = new ProductCategoryCacheDTO();
								productCategory.putValue(fdpProductCategoryDTO.getProductCategoryName(), fdpProductCategoryDTO);
								fdpCache.putValue(metaBag, productCategory);
							}
						}
						//for new cache
						ProductCouponMapCacheDTO cacheDTO = new ProductCouponMapCacheDTO();
						cacheDTO.setProductCouponMap(productCouponMap);
						fdpCache.putValue(metaBagMap, cacheDTO);

					}
				}
			}
		} catch (FDPConcurrencyException e) {
			LOGGER.error(e.getMessage());
			throw new FDPServiceException(e);
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle is mandatory");
		} else {
			try {
				List<FDPProductCategoryMappingDTO> mappingDTOs = new ArrayList<FDPProductCategoryMappingDTO>();
				List<FDPProductCategoryDTO> categoryDTOs = fdpProductCategoryDAO.getProductCategories(fdpCircle.getCircleId());
				Map<Long, List<String>> productCouponMap = fdpProductCategoryMappingDAO.getAllMappings();
				FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_CATEGORY, null);
				FDPMetaBag metaBagMap = new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_COUPON_MAP, null);
				if(categoryDTOs != null){
					ProductCategoryCacheDTO productCategory = new ProductCategoryCacheDTO();
					for(FDPProductCategoryDTO fdpProductCategoryDTO : categoryDTOs){
						mappingDTOs = fdpProductCategoryMappingDAO.getMappings(fdpProductCategoryDTO.getProductCategoryId());
						List<String> productIdStr = new ArrayList<String>();
						for(FDPProductCategoryMappingDTO categoryMappingDTO : mappingDTOs){
							productIdStr.add(String.valueOf(categoryMappingDTO.getProductId().getProductId()));
						}
						fdpProductCategoryDTO.setProductIds(productIdStr);
						productCategory.putValue(fdpProductCategoryDTO.getProductCategoryName(), fdpProductCategoryDTO);
						fdpCache.putValue(metaBag, productCategory);
					}
				}
				//for new cache
				ProductCouponMapCacheDTO cacheDTO = new ProductCouponMapCacheDTO();
				cacheDTO.setProductCouponMap(productCouponMap);
				fdpCache.putValue(metaBagMap, cacheDTO);

			} catch (FDPConcurrencyException e) {
				LOGGER.error(e.getMessage());
				throw new FDPServiceException(e);
			}
		}
		return true;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.PRODUCT_CATEGORY;
	}

}
