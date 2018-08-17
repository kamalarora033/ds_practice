package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.infinispan.client.hotrod.RemoteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.MetaDataCache;
import com.ericsson.fdp.business.cache.datageneration.service.ProductNameDataService;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.product.impl.ProductNameCacheImpl;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.product.ProductDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;

@Stateless(mappedName = "productNameDataService")
public class ProductNameDataServiceImpl implements ProductNameDataService{

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProductNameDataServiceImpl.class);

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;
	
	
	
	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		try{
			Long productId = null;
			FDPCircle fdpCircle = null;
			ActionTypeEnum action = null;
			String productName = null;
			if (null != updateCacheDTO) {
				productId = updateCacheDTO.getId();
				fdpCircle = updateCacheDTO.getCircle();
				action = updateCacheDTO.getAction();
				productName = (updateCacheDTO.getProductName() !=null ? updateCacheDTO.getProductName() : null);
			}	
			updateCache(productId,fdpCircle,action, productName);
		} catch (final ExpressionFailedException efe) {
			LOGGER.error(efe.getMessage());
			throw new FDPServiceException(efe);
		}catch (ExecutionFailedException e) {
			LOGGER.error(e.getMessage());
			throw new FDPServiceException(e);
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle is mandatory");
		} else {
			try {
				this.updateCache(null,fdpCircle,null, null);
			} catch (ExpressionFailedException e) {
				LOGGER.error(e.getMessage());
				throw new FDPServiceException(e);
			}catch (ExecutionFailedException e) {
				LOGGER.error(e.getMessage());
				throw new FDPServiceException(e);
			}
		}
		return true;
	}
	
	private void updateCache(final Long productId,final FDPCircle fdpCircle,final ActionTypeEnum action, String productName)
			throws FDPServiceException, ExpressionFailedException,ExecutionFailedException{
		final List<ProductDTO> productDTOs;
		
		if (productId == null)
		{
		   productDTOs=entityService.getLaunchedProducts(fdpCircle);
		   if (!productDTOs.isEmpty()) {
			   for (final ProductDTO productDTO : productDTOs) {
					this.initializeUpdateProduct(productDTO, fdpCircle);
				}
		   }
		  
		}else{
			if (action.getValue() == ActionTypeEnum.DELETE.getValue()) {
				if (productName != null) {
					this.updateDeleteProductNameIDMap(null, fdpCircle,action, productName);
				}
				
			} else if (action.getValue() == ActionTypeEnum.ADD_UPDATE.getValue()) {
				productDTOs=entityService.getLaunchedProducts(productId);
			       for (final ProductDTO productDTO : productDTOs) {
						this.updateDeleteProductNameIDMap(productDTO, fdpCircle,action, productDTO.getProductInfoDTO().getProductName());
					}
			} 
		}
	}

	private void updateDeleteProductNameIDMap(final ProductDTO productDTO,final FDPCircle fdpCircle,final ActionTypeEnum action, String name) 
			throws ExpressionFailedException,ExecutionFailedException{
		
		final FDPCircle fdpCircleToSet = null != fdpCircle ? fdpCircle: productDTO.getFdpCircle();
		final FDPCache<FDPMetaBag, FDPCacheable> fdpMetaDataCache;
		try{
			fdpMetaDataCache = ApplicationConfigUtil.getMetaDataCache();
		}catch(Exception e) {
			throw new ExecutionFailedException("Something went wrong in executing for product id: "+ name);
		}
		// Remove product information from cache
		if (action.getValue() == ActionTypeEnum.DELETE.getValue()) {
			fdpCache.removeKey(new FDPMetaBag(fdpCircleToSet,ModuleType.PRODUCT_NAME_ID_MAP,name));
		} 
		else{
			// update PRODUCT_NAME_ID_MAP cache when a new product is added or updated from admin gui
			final FDPCacheable fdpProductCacheable = fdpMetaDataCache.getValue(new FDPMetaBag(fdpCircleToSet,ModuleType.PRODUCT_NAME_ID_MAP, name));
			Map<Object,Object> productList = null;
			productList = MetaDataCache.getAllValue(ModuleType.PRODUCT_NAME_ID_MAP);
			//final RemoteCache<Object, Object> remoteCache = MetaDataCache.getCacheStoreForUpdate(ModuleType.PRODUCT_NAME_ID_MAP);
			final String productIdValue = productDTO.getProductInfoDTO().getProductIdValue();
			ProductNameCacheImpl productNameCacheImpl = null;
			if(fdpProductCacheable == null) // value not found wrt key, due to key is changed
			{
				//final FDPMetaBag metaBag = new FDPMetaBag(fdpCircleToSet,ModuleType.PRODUCT_NAME_ID_MAP,productName);
				boolean productIdValueExists = false;
				for(Map.Entry<Object, Object> rc : productList.entrySet() ){
					String key = (String)rc.getKey();
					FDPCacheable value = (FDPCacheable) rc.getValue();
					if (value instanceof ProductNameCacheImpl) {
						productNameCacheImpl = (ProductNameCacheImpl) value;
						if(productNameCacheImpl.getProductIdValue().equalsIgnoreCase(productIdValue)){
							productIdValueExists = true;
							key = key.substring(key.indexOf("PRODUCT_NAME_ID_MAP")+"PRODUCT_NAME_ID_MAP".length()+1);
							fdpCache.removeKey(new FDPMetaBag(fdpCircleToSet,ModuleType.PRODUCT_NAME_ID_MAP,key));
							putIdValueinCache(fdpCircleToSet,name,productIdValue);
							break;
						}
					}
					
				}
				if(!productIdValueExists) // when product name and productId value not exists
				{
					putIdValueinCache(fdpCircleToSet,name,productIdValue);
				}
			} else {
				
				String productIdValueOfKey = null;
				if (fdpProductCacheable instanceof ProductNameCacheImpl) {
					productNameCacheImpl = (ProductNameCacheImpl) fdpProductCacheable;
					productIdValueOfKey = productNameCacheImpl.getProductIdValue();
					if(!productIdValueOfKey.equalsIgnoreCase(productIdValue)){
						putIdValueinCache(fdpCircleToSet,name,productIdValue);
					}

				}
			}	
		} 
	}
	
	
	//Method to put value in cache if product id value changed 
	private void putIdValueinCache(final FDPCircle fdpCircleToSet,String productName,String productIdValue){
		ProductNameCacheImpl productNameCacheImpl = null;
		final FDPMetaBag metaBag = new FDPMetaBag(fdpCircleToSet,ModuleType.PRODUCT_NAME_ID_MAP,productName);
		productNameCacheImpl = new ProductNameCacheImpl(productIdValue);
		fdpCache.putValue(metaBag, productNameCacheImpl);
	}
	/**
	 * Initialize update product.
	 *
	 * @param productDTO
	 *            the product dto
	 * @param fdpCircle
	 *            the fdp circle
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private void initializeUpdateProduct(final ProductDTO productDTO,final FDPCircle fdpCircle) throws ExpressionFailedException {
		final String productName = productDTO.getProductInfoDTO().getProductName();
		final String productIdValue = productDTO.getProductInfoDTO().getProductIdValue();
		final FDPCircle fdpCircleToSet = null != fdpCircle ? fdpCircle: productDTO.getFdpCircle();
		//final FDPMetaBag metaBag = new FDPMetaBag(fdpCircleToSet,ModuleType.PRODUCT_NAME_ID_MAP, productName);
		if(!("null".equals(productIdValue)) && productIdValue.length()>0){
			putIdValueinCache(fdpCircleToSet,productName,productIdValue);
			/*ProductNameCacheImpl productNameCache = new ProductNameCacheImpl(productIdValue);
			fdpCache.putValue(metaBag, productNameCache);*/
		}

		// logs added at info level
		final StringBuilder messageStr = new StringBuilder();
		messageStr.append(" Product information updated in cache successfully.").append(System.lineSeparator())
				.append("General ID Value Mapping: Id = ")
				.append(productIdValue);
		
		LOGGER.info("{} : {} : {}", new Object[] { this.getClass().getName(),
				"initializeUpdateProduct", messageStr });
	}
	
	
	@Override
	public ModuleType getModuleType() {
		return ModuleType.PRODUCT_NAME_ID_MAP;
	}

}
