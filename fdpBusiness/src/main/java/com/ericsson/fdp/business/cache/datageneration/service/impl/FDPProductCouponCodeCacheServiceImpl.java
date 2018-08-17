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
import com.ericsson.fdp.business.cache.datageneration.service.FDPProductCouponCodeCacheService;
import com.ericsson.fdp.business.product.ProductCouponCache;
import com.ericsson.fdp.business.vo.ProductCouponMapCacheDTO;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.product.ProductCategoryDTO;
import com.ericsson.fdp.dao.dto.product.ProductCouponDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.dao.fdpadmin.FDPProductCategoryMappingDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPProductCouponDAO;

@Stateless(mappedName = "fDPProductCouponCodeCacheService")
public class FDPProductCouponCodeCacheServiceImpl implements FDPProductCouponCodeCacheService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	@Inject
	private FDPProductCouponDAO productCouponDAO;

	@Inject
	FDPProductCategoryMappingDAO fdpProductCategoryMappingDAO;

	/** The Logger */
	private static final Logger LOGGER = LoggerFactory.getLogger(FDPProductCouponCodeCacheServiceImpl.class);

	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		LOGGER.info("updating dto :: product coupon");
		try {
			Long productCouponId = updateCacheDTO.getId();
			FDPCircle fdpCircle = updateCacheDTO.getCircle();
			Map<Long, List<String>> productCouponMap = fdpProductCategoryMappingDAO.getAllMappings();
			FDPMetaBag metaBagMap = new FDPMetaBag(updateCacheDTO.getCircle(), ModuleType.PRODUCT_COUPON_MAP, null);

			LOGGER.info("above ADD_UPDATE action :: product coupon");
			if(ActionTypeEnum.ADD_UPDATE.equals(updateCacheDTO.getAction())) {
				LOGGER.info("When action is add_update");

				if(updateCacheDTO.getUiObjectDTO() instanceof ProductCouponDTO){
					ProductCouponDTO couponDTO = (ProductCouponDTO) updateCacheDTO.getUiObjectDTO();

					List<ProductCouponDTO> productCouponList = new ArrayList<ProductCouponDTO>();
					productCouponList.add(couponDTO);
					updateCache(productCouponList, fdpCircle);
				}

				//for new cache
				ProductCouponMapCacheDTO cacheDTO = new ProductCouponMapCacheDTO();
				cacheDTO.setProductCouponMap(productCouponMap);
				fdpCache.putValue(metaBagMap, cacheDTO);
				LOGGER.info("Testing product coupon :: update");
				testCache(fdpCircle, "Update");
				testNewCache(fdpCircle, "Update");
			}

		}
		catch (Exception e) {
			LOGGER.error(e.getMessage());
			throw new FDPServiceException(e);
		}
		return true;
	}



	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {

		LOGGER.info("Initialize Cache for Product Coupon");
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle is mandatory");
		} else {
			try {
				List<ProductCouponDTO> productCouponList = productCouponDAO.getListCMSProductCode(fdpCircle.getCircleId());
				Map<Long, List<String>> productCouponMap = fdpProductCategoryMappingDAO.getAllMappings();
				FDPMetaBag metaBagMap = new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_COUPON_MAP, null);

				if(productCouponList != null){
					LOGGER.info("Product Coupon List is not null");
					final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_COUPON_CODE, null);
					ProductCouponCache productCouponCache = new ProductCouponCache();
					productCouponCache.putValue(productCouponList);
					fdpCache.putValue(metaBag, productCouponCache);
				}
				else{
					LOGGER.info("Product Coupon is null");
				}

				//for new cache
				ProductCouponMapCacheDTO cacheDTO = new ProductCouponMapCacheDTO();
				cacheDTO.setProductCouponMap(productCouponMap);
				fdpCache.putValue(metaBagMap, cacheDTO);
				//-------------------------------------------------------------------
				testCache(fdpCircle, "Initialize");
				testNewCache(fdpCircle, "Initialize");

			} catch (Exception e) {
				LOGGER.error("Error while caching product coupon");
				LOGGER.error(e.getMessage());
				throw new FDPServiceException(e);
			}
		}
		return true;
	}

	private void testNewCache(FDPCircle fdpCircle, String string) {
		final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_COUPON_MAP, null);
		final FDPCacheable fdpCacheable = fdpCache.getValue(metaBag);


		ProductCouponMapCacheDTO productCouponCache = (ProductCouponMapCacheDTO) fdpCacheable;
		Map<Long, List<String>> cah = productCouponCache.getProductCouponMap();
		if(cah !=null){
			LOGGER.info("Cah is not null");
			for(Long key : cah.keySet()){
				LOGGER.info("KEY :: "+key);
				List<String> st = cah.get(key);
				for(String s : st){
					LOGGER.info("Value is :: "+s);
				}
			}
		}


	}



	private void testCache(FDPCircle fdpCircle, String mode) {
	
			LOGGER.info("Mode is :: "+mode);
			//System.out.println("Mode is :: "+mode);
			LOGGER.info("To Test product coupon");
			LOGGER.info("Cache updated successfully for circle :: "+fdpCircle.getCircleName());
			final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_COUPON_CODE, null);
			final FDPCacheable fdpCacheable = fdpCache.getValue(metaBag);
			ProductCouponCache productCouponCache = (ProductCouponCache) fdpCacheable;
			LOGGER.info("Printing Values :: ");
			Map<String, ProductCouponDTO> mapToTest = productCouponCache.getProductCouponMap();
			for(String key : mapToTest.keySet()){
				LOGGER.info("mapToTest("+key+") value is :: "+mapToTest.get(key));
				ProductCouponDTO productCouponDTO = mapToTest.get(key);
				LOGGER.info("code :: "+productCouponDTO.getCmsProductCode());
				LOGGER.info("Notifcation :: "+productCouponDTO.getNotifSuccess());
				LOGGER.info("Id :: "+productCouponDTO.getCmsProductCodeId());
				List<ProductCategoryDTO> categoryDTO = productCouponDTO.getProductCategoryDTO();
				for(ProductCategoryDTO cat : categoryDTO){
					LOGGER.info("cat Id :: "+cat.getProductCategoryId());
					LOGGER.info("cat name :: "+cat.getProductCategoryName());
				}
			}
			LOGGER.info("Value of cache :: "+fdpCache.getValue(metaBag));
	}



	private void updateCache(
			List<ProductCouponDTO> productCouponList, FDPCircle fdpCircle) throws FDPServiceException {

		final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.PRODUCT_COUPON_CODE, null);
		final FDPCacheable fdpCacheable = fdpCache.getValue(metaBag);

		if(fdpCacheable!= null){
			if (fdpCacheable instanceof ProductCouponCache) {
				ProductCouponCache productCouponCache = (ProductCouponCache) fdpCacheable;
				productCouponCache.putValue(productCouponList);
				fdpCache.putValue(metaBag, productCouponCache);
			}
		}	
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.PRODUCT_COUPON_CODE;
	}

}
