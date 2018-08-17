package com.ericsson.fdp.business.enums.ivr.commandservice;

/**
 * The Enum IVRCommandServiceResponseKey.
 * 
 * @author Ericsson
 */
public enum FulfillmentResponseKey {

	/*
	 * {"status":”failure”,"responseCode":<responseCode>,
	 * responseDescription:<description>,systemType:<SystemName such as FDP>,
	 * responseValue:<optional, response xml for failure depending on failure is
	 * after response or before>}
	 */

	/** The status. */
	STATUS("status"),

	/** The response code. */
	RESPONSE_CODE("responseCode"),

	/** The response description. */
	RESPONSE_DESCRIPTION("responseDescription"),

	/** The system type. */
	SYSTEM_TYPE("systemType"),

	/** The response value. */
	RESPONSE_VALUE("responseValue");

	/** The value. */
	private String value;

	/**
	 * Instantiates a new iVR command service response key.
	 * 
	 * @param value
	 *            the value
	 */
	private FulfillmentResponseKey(final String value) {
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
