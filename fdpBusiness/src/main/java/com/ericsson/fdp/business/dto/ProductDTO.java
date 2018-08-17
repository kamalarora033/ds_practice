package com.ericsson.fdp.business.dto;

public class ProductDTO {

	/** The productId. */
	private String productId;
	
	private String productName;
	
	private String productPrice;
	
	private String paySrc;
	
	private String srcChannel;
	
	private String activationDate;
	
	private String expiryDate;
	
	private String activatedBy;

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(String productPrice) {
		this.productPrice = productPrice;
	}

	public String getPaySrc() {
		return paySrc;
	}

	public void setPaySrc(String paySrc) {
		this.paySrc = paySrc;
	}

	public String getSrcChannel() {
		return srcChannel;
	}

	public void setSrcChannel(String srcChannel) {
		this.srcChannel = srcChannel;
	}

	public String getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(String activationDate) {
		this.activationDate = activationDate;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getActivatedBy() {
		return activatedBy;
	}

	public void setActivatedBy(String activatedBy) {
		this.activatedBy = activatedBy;
	}
	
}
