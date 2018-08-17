package com.ericsson.fdp.business.product.impl;

import com.ericsson.fdp.business.product.ProductNameCache;

public class ProductNameCacheImpl implements ProductNameCache{


	private static final long serialVersionUID = 4275918201730376523L;

	/**
	 * The product id.
	 */
	private String productIdValue;

	public ProductNameCacheImpl(String productIdValue) {
		this.productIdValue = productIdValue;
	}

	public String getProductIdValue() {
		return productIdValue;
	}
	
	public void setProductIdValue(String productIdValue) {
		this.productIdValue = productIdValue;
	}


}
