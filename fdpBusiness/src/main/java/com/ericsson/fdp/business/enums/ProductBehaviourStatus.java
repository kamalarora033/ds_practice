package com.ericsson.fdp.business.enums;

/**
 * The product behaviour status
 * 
 * @author Ericsson
 * 
 */
public enum ProductBehaviourStatus {

	/**
	 * The requested state.
	 */
	REQUESTED("Requested"),
	/**
	 * The confirmed state.
	 */
	CONFIRMED("Confirmed"),
	/**
	 * The declined state.
	 */
	DECLINED("Declined");

	/**
	 * The status of product.
	 */
	private String status;

	/**
	 * The method is used to get the status.
	 * 
	 * @return the status.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Constructor.
	 * 
	 * @param status
	 *            the status string.
	 */
	private ProductBehaviourStatus(final String status) {
		this.status = status;
	}

}
