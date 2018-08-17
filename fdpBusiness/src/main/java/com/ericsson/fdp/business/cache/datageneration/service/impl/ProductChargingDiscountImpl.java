package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.ProductChargingDiscount;
import com.ericsson.fdp.business.charging.Discount;
import com.ericsson.fdp.business.charging.impl.ChargingDiscount;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.util.ExpressionUtil;
import com.ericsson.fdp.common.enums.DiscountTypes;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.FDPConfigurationDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.entity.FDPProductChargingDiscount;
import com.ericsson.fdp.dao.fdpadmin.FDPChargingDiscountDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPConfigurationDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPProductDiscountConstraintDAO;

@Stateless(mappedName = "ProductChargingDiscountImpl")
public class ProductChargingDiscountImpl implements ProductChargingDiscount {

	//Fetch all discount details from database (DAO).
	//No need of DTO here.
	//Convert Entity to Discount object -->ChargingDiscount.java
	//Create a new method in ExpressionUtil to convert into Expression Object.
	//Put in cache key=productId and value=ChargingDiscount.java
	
	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;
	
	@Inject
	FDPChargingDiscountDAO chargingDiscountDAO;

	@Inject
	FDPProductDiscountConstraintDAO fdpProductDiscountConstraintDAO;
	
	@Inject
    FDPConfigurationDAO configurationDAO;
	
	@Override
	public boolean updateMetaCache(UpdateCacheDTO updateCacheDTO)
			throws FDPServiceException {
		this.initializeUpdateDiscountCache(updateCacheDTO.getCircle(), updateCacheDTO.getId());
		return true;
	}

	@Override
	public boolean initializeMetaCache(FDPCircle fdpCircle)
			throws FDPServiceException {
		this.initializeUpdateDiscountCache(fdpCircle, null);
		return true;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.PRODUCT_CHARGING_DISCOUNT;
	}
	
	/**
	 * Prepares key.
	 * 
	 * @param key
	 * @param fdpCircle
	 * @return
	 */
	private FDPMetaBag getFDPMetaBag(final Long key, final FDPCircle fdpCircle) {
		//System.out.println("Key::::: "+key);
		return new FDPMetaBag(fdpCircle, getModuleType(), key);
	}
	
	/**
	 * Prepares value.
	 * 
	 * @param discountConstraint
	 * @return
	 */
	private Discount getDiscountValue(final FDPProductChargingDiscount discountConstraint) throws FDPServiceException{
		Discount discount = null;
        // try {
			//fdpProductDiscountConstraintStepDAO.get			
        // Expression expression = ExpressionUtil.prepareProductDiscountExpression(
        // fdpProductDiscountConstraintDAO.getFdpProductDiscount(discountConstraint.getProductId().getProductId()));
			/*DiscountTypes discountType =null;
			Long discountValue = null;*/
			 //DiscountTypes discountType=null;
			//if(null!= discountConstraint && null!= discountConstraint.getProductChargingDiscount() && null!=discountConstraint.getProductChargingDiscount().getDiscountType()){
			/*for(final FDPDiscountConstraint fdpDiscountConstraint:discountConstraint.getFdpDiscountConstraintsSet())	{
				discountType = DiscountTypes.getDiscountTypes(fdpDiscountConstraint.getProductChargingDiscount()
						.getDiscountType());
				discountValue = fdpDiscountConstraint.getProductChargingDiscount().getDiscountValue();
			}*/
		/*	discountType = DiscountTypes.getDiscountTypes(discountConstraint.getDiscountType());
			discountValue = discountConstraint.getDiscountValue();*/
			
			
			//final DiscountTypes 
				//getProductChargingDiscount().getDiscountType());
			/*}else{
				System.out.println("Null value of discountConstraint.getProductChargingDiscount().getDiscountType()=========>>>>>>>>>>");
				System.out.println(discountConstraint.toString() + " : 1");
				System.out.println(discountConstraint.getProductChargingDiscount().toString() + " : 2");
				System.out.println(discountConstraint.getProductChargingDiscount().getDiscountType().toString() + " : 3");
			}*/
			/*if(null != expression 
					&& null != discountConstraint.getDiscountType() && null != discountConstraint.getDiscountValue())*/
			
        try {
            Expression expression = ExpressionUtil.prepareProductDiscountExpression(fdpProductDiscountConstraintDAO
                    .getFdpProductDiscount(discountConstraint.getProductId().getProductId()));

            if (null != discountConstraint.getDiscountType() && null != discountConstraint.getDiscountValue()) {
                discount = new ChargingDiscount(expression, DiscountTypes.getDiscountTypes(discountConstraint.getDiscountType()),
                        discountConstraint.getDiscountValue());
/*                
                DiscountTypes type = DiscountTypes.getDiscountTypes(discountConstraint.getDiscountType());
                
                if (DiscountTypes.FIXED_DISCOUNT.equals(type)) {
                    FDPConfigurationDTO configurationDTO = configurationDAO.getFDPConfigurationsForCircle(discountConstraint.getProductId()
                            .getCircleId().getCircleId(),
                            ConfigurationKey.CS_CONVERSION_FACTOR.getAttributeName());
                    discount = new ChargingDiscount(expression, DiscountTypes.getDiscountTypes(discountConstraint.getDiscountType()),
                            discountConstraint.getDiscountValue() * Long.valueOf(configurationDTO.getAttributeValue()));
                } else {
                    discount = new ChargingDiscount(expression, DiscountTypes.getDiscountTypes(discountConstraint.getDiscountType()),
                            discountConstraint.getDiscountValue());
                }
                */
            }
        } catch (ExpressionFailedException e) {
            e.printStackTrace();
            throw new FDPServiceException("Unable to load cache.", e);
        }
        //System.out.println("Value::::: " + discount);
        return discount;
	}
	
	private void initializeUpdateDiscountCache(final FDPCircle fdpCircle, final Long productID) throws FDPServiceException{
		if(null == productID){
			final List<FDPProductChargingDiscount> fdpDiscountConstraints = chargingDiscountDAO.getDiscountConstraint(fdpCircle.getCircleId());
			for (final FDPProductChargingDiscount discountConstraint : fdpDiscountConstraints) {
					/*fdpCache.putValue(getFDPMetaBag(discountConstraint.getChargingDiscountId(), fdpCircle),
								getDiscountValue(discountConstraint));*/
				
				fdpCache.putValue(getFDPMetaBag(discountConstraint.getProductId().getProductId(), fdpCircle),
						getDiscountValue(discountConstraint));
			}
		}else{
			final List<FDPProductChargingDiscount> fdpDiscountConstraints = chargingDiscountDAO.getDiscountConstraintByProductID(productID);
			//final List<FDPDiscountConstraint> fdpDiscountConstraints = fdpProductDiscountConstraintDAO.getFdpProductDiscount(productID);
				for(final FDPProductChargingDiscount discountConstraint : fdpDiscountConstraints){
					
					fdpCache.putValue(getFDPMetaBag(discountConstraint.getProductId().getProductId(), fdpCircle),
							getDiscountValue(discountConstraint));	
					
		/*				fdpCache.putValue(getFDPMetaBag(discountConstraint.getChargingDiscountId(), fdpCircle),
									getDiscountValue(discountConstraint));				
		*/		}
		}
	}
}	