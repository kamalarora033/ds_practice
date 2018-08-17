package com.ericsson.fdp.business.enums;

public enum TariffEnquiryConditionProvider {

	/**
	 * No Operation Condition.
	 */
	NOP(1, "No Operation"),

	/**
	 * Less Than Condition.
	 */
	LT(2, "Less than (<)"),

	/**
	 * Greater Than Condition.
	 */
	GT(3, "Greater than or equal (> ,=)"),

	/**
	 * Equals Condition.
	 */
	EQ(4, "Equal to (=)"),

	/**
	 *  Greater than and less than Condition.
	 */
	GTLT(5, "Greater than and less than");

	/** The value. */
	Integer value;

	/** The condition text. */
	String conditionText;

	private TariffEnquiryConditionProvider(Integer value, String conditionType) {
		this.value = value;
		this.conditionText = conditionType;
	}

	/**
	 * This method will return TariffEnquiryConditionProvider type for condition
	 * type.
	 * 
	 * @param conditionType
	 * @return
	 */
	public static TariffEnquiryConditionProvider getTariffEnquiryConditionType(int conditionType) {
		TariffEnquiryConditionProvider cType = null;
		for (TariffEnquiryConditionProvider conditionProvider : TariffEnquiryConditionProvider.values()) {
			if (conditionProvider.getValue() == conditionType) {
				cType = conditionProvider;
				break;
			}
		}
		return cType;
	}

	/**
	 * @return the value
	 */
	public Integer getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Integer value) {
		this.value = value;
	}

	/**
	 * @return the conditionText
	 */
	public String getConditionText() {
		return conditionText;
	}

	/**
	 * @param conditionText
	 *            the conditionText to set
	 */
	public void setConditionText(String conditionText) {
		this.conditionText = conditionText;
	}

}
