package com.ericsson.fdp.business.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.dao.dto.product.ProductCouponDTO;

/**
 * The interface that provides access to methods required by product coupon.
 *
 * @author ericsson
 *
 */
public class ProductCouponCache implements FDPCacheable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8157901215080435512L;

	private Map<String, ProductCouponDTO> productCouponMap;


	public Map<String, ProductCouponDTO> getProductCouponMap() {
		return productCouponMap;
	}

	public void setProductCouponMap(Map<String, ProductCouponDTO> productCouponMap) {
		this.productCouponMap = productCouponMap;
	}

	public void putValue(List<ProductCouponDTO> couponDTOList) throws FDPServiceException {
		try {
			if(productCouponMap == null){
				productCouponMap = new HashMap<String, ProductCouponDTO>();
			}

			for(ProductCouponDTO couponDTO : couponDTOList){
				productCouponMap.put(couponDTO.getCmsProductCode().toLowerCase(), couponDTO);
			}
		}
		catch(Exception e){
			throw new FDPServiceException(e);
		}
	}

}
