package com.ericsson.fdp.business.recurringservice;

/**
 * This enum used for differentiating the active products type
 * 
 * 
 */
public enum ActiveProductType {
	PAM("PAM"), RS("RS");
	private ActiveProductType(String productType) {
		this.productType = productType;
	}

	private String productType;

	public String getProductType() {
		return productType;
	}

	public void setProductType(String productType) {
		this.productType = productType;
	}


}
