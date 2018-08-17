package com.ericsson.fdp.business.vo;

import java.io.Serializable;

import com.ericsson.fdp.business.product.Product;

public class FDPActiveServicesVO implements Serializable {

	/**
	 * The Class Serial Version UID.
	 */
	private static final long serialVersionUID = 2251600198413667904L;

	/**
	 * The Product.
	 */
	private Product product;

	/**
	 * The Expiry date.
	 */
	private String serviceId;
	
	private String activationDate;
	
	private String lastRenewalDate;
	
	private String nextRenewalDate;
	
	private String renewalPeriod;

	public FDPActiveServicesVO() {
		
	}

	
	public FDPActiveServicesVO(final Product product, final String serviceId) {
		this.product = product;
		this.serviceId = serviceId;
	}

	/**
	 * @return the product
	 */
	public Product getProduct() {
		return product;
	}

	/**
	 * @return the serviceId
	 */
	public String getServiceId() {
		return serviceId;
	}

	public String getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(String activationDate) {
		this.activationDate = activationDate;
	}

	public String getLastRenewalDate() {
		return lastRenewalDate;
	}

	public void setLastRenewalDate(String lastRenewalDate) {
		this.lastRenewalDate = lastRenewalDate;
	}

	public String getNextRenewalDate() {
		return nextRenewalDate;
	}

	public void setNextRenewalDate(String nextRenewalDate) {
		this.nextRenewalDate = nextRenewalDate;
	}

	public String getRenewalPeriod() {
		return renewalPeriod;
	}

	public void setRenewalPeriod(String renewalPeriod) {
		this.renewalPeriod = renewalPeriod;
	}
	
	public void setProduct(Product product) {
		this.product = product;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	@Override
	public String toString() {
		return "FDPActiveServicesVO [product=" + product + ", serviceId="
				+ serviceId + ", activationDate=" + activationDate
				+ ", lastRenewalDate=" + lastRenewalDate + ", nextRenewalDate="
				+ nextRenewalDate + ", renewalPeriod=" + renewalPeriod + "]";
	}



}