package com.ericsson.ms.common.enums;

/**
 * The Request Values Enum
 * 
 * @author Ericsson
 *
 */
public enum RequestValuesEnum {

	/** The Action */
	ACTION("action",true),
	/** The Product Name */
	PRODUCT_NAME("productName", false),
	/** The Product Id */
	PRODUCT_ID("productId", false),
	/** The MSISDN */
	MSISDN("msisdn", true),
	/** The Expiry */
	EXPIRY_DATE("expiryDate", true),
	/** The Amount */
	AMOUNT("amount", true);

	/** The Value */
	private String value;

	/** The Mandatory */
	private boolean mandatory;

	/**
	 * 
	 * @param value
	 * @param mandetory
	 */
	private RequestValuesEnum(String value, boolean mandatory) {
		this.value = value;
		this.mandatory = mandatory;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @return the mandatory
	 */
	public boolean isMandatory() {
		return mandatory;
	}

}
