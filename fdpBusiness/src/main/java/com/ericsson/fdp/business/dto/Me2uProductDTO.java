package com.ericsson.fdp.business.dto;

import java.util.HashMap;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.product.Product;

public class Me2uProductDTO implements FDPCacheable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 32673642472589L;

	private Product product;
	
	private String availableBalance;
	
	private String expiryDate; // Used in USSD
	
	private String offerID;
	
	private String DAID;
	
	private HashMap<String, Long> daInstance;
	
	private String accountValue;
	
	private String expiryDateAPI;

	public String getAccountValue() {
		return accountValue;
	}

	public void setAccountValue(String accountValue) {
		this.accountValue = accountValue;
	}

	public String getAvailableBalance() {
		return availableBalance;
	}

	public void setAvailableBalance(String availableBalance) {
		this.availableBalance = availableBalance;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getOfferID() {
		return offerID;
	}

	public void setOfferID(String offerID) {
		this.offerID = offerID;
	}

	public String getDAID() {
		return DAID;
	}

	public void setDAID(String dAID) {
		DAID = dAID;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public HashMap<String, Long> getDaInstance() {
		return daInstance;
	}

	public void setDaInstance(HashMap<String, Long> daInstance) {
		this.daInstance = daInstance;
	}

	public String getExpiryDateAPI() {
		return expiryDateAPI;
	}

	public void setExpiryDateAPI(String expiryDateAPI) {
		this.expiryDateAPI = expiryDateAPI;
	}

}
