package com.ericsson.ms.http.model;

import java.io.Serializable;

/**
 * 
 * @author Ericsson
 * 
 */

public class MSHttpRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2843153498813855746L;
	/** The Request Id */
	private String requestId;
	/** The Action */
	private String action;
	/** The Product Name */
	private String productName;
	/** The Product Id */
	private String productID;
	/** The MSISDN */
	private String msisdn;
	/** The Expiry date */
	private String expiryDate;
	/** The Amount */
	private String amount;

	/**
	 * @return the requestId
	 */
	public String getRequestId() {
		return requestId;
	}

	/**
	 * @param requestId
	 *            the requestId to set
	 */
	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName
	 *            the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return the productID
	 */
	public String getProductID() {
		return productID;
	}

	/**
	 * @param productID
	 *            the productID to set
	 */
	public void setProductID(String productID) {
		this.productID = productID;
	}

	/**
	 * @return the msisdn
	 */
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * @param msisdn
	 *            the msisdn to set
	 */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * @return the expiryDate
	 */
	public String getExpiryDate() {
		return expiryDate;
	}

	/**
	 * @param expiryDate
	 *            the expiryDate to set
	 */
	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	/**
	 * @return the amount
	 */
	public String getAmount() {
		return amount;
	}

	/**
	 * @param ammount
	 *            the amount to set
	 */
	public void setAmount(String amount) {
		this.amount = amount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "HttpRequestParams [requestId=" + requestId + ", action=" + action + ", productName=" + productName
				+ ", productID=" + productID + ", msisdn=" + msisdn + ", expiryDate=" + expiryDate + ", amount="
				+ amount + "]";
	}

}
