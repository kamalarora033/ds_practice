package com.ericsson.fdp.business.enums.ivr;

public enum FulfillmentResponseTypes {

	/** The product **/
	PRODUCT("Product"),

	/** The Menu **/
	MENU("Menu"),

	/** The others **/
	OTHERS("Others");

	/** The value. */
	private String value;

	/**
	 * Instantiates a new iVR resposne type.
	 * 
	 * @param value
	 *            the value
	 */
	private FulfillmentResponseTypes(final String value) {
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
