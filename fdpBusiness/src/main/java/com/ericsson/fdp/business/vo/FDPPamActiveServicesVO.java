package com.ericsson.fdp.business.vo;

import java.io.Serializable;
import com.ericsson.fdp.business.product.Product;


/**
 * The Class FDPPamActiveServicesVO.
 */
public class FDPPamActiveServicesVO implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 955351896447256887L;
	
	/**
	 * The Product.
	 */
	private Product product;
	
	/** The pam id. */
	private String pamId;

	private String lastRenewalDate;
	
	private String nextRenewalDate;
	
	/**
	 * Instantiates a new fDP pam active services vo.
	 *
	 * @param product the product
	 * @param pamId the pam id
	 */
	public FDPPamActiveServicesVO(Product product, String pamId) {
		this.product = product;
		this.pamId = pamId;
	}

	/**
	 * Gets the product.
	 *
	 * @return the product
	 */
	public Product getProduct() {
		return product;
	}

	/**
	 * Gets the pam id.
	 *
	 * @return the pam id
	 */
	public String getPamId() {
		return pamId;
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


	@Override
	public String toString() {
		return "FDPPamActiveServicesVO [product=" + product + ", pamId=" + pamId + ", lastRenewalDate="
				+ lastRenewalDate + ", nextRenewalDate=" + nextRenewalDate + "]";
	}
	
	
}
