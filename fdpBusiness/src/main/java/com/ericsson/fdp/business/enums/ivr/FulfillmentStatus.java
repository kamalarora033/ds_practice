package com.ericsson.fdp.business.enums.ivr;

/**
 * The Enum IVRStatus.
 * 
 * @author Ericsson
 */
public enum FulfillmentStatus {

	/** The success. */
	SUCCESS("SUCCESS"),

	/** The failure. */
	FAILURE("FAILURE");

	/** The value. */
	private String value;

	/**
	 * Instantiates a new iVR status.
	 * 
	 * @param value
	 *            the value
	 */
	private FulfillmentStatus(final String value) {
		this.value = value;
	}

	/**
	 * Gets the value.
	 * 
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
