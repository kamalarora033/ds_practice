package com.ericsson.fdp.business.enums;

/**
 * This enum defines all the error codes that can occur while importing a file
 * for shared account.
 * 
 * @author Ericsson
 * 
 */
public enum ImportErrorCode {
	/**
	 * In case the row type cannot be identified.
	 */
	ROW_SHARED_TYPE_UNDEFINED("The row does not contain provider or consumer information"),
	/**
	 * In case row contains partial information on the parent.
	 */
	PARTIAL_PARENT_INFORMATION_PRESENT("The row contains partial information on parent."),
	/**
	 * In case the parent offer id is invalid.
	 */
	PARENT_OFFERID_NOT_VALID("The group id is not valid."),
	/**
	 * In case the parent msisdn is invalid.
	 */
	PARENT_GROUPMSISDN_NOT_VALID("The group msisdn is not valid."),
	/**
	 * In case the parent for the provided msisdn and offer id does not exist.
	 */
	PARENT_NOT_PRESENT("The parent for the provided msisdn and offer does not exist"),
	/**
	 * In case the provided msisdn is invalid.
	 */
	MSISDN_INVALID("The msisdn is invalid"),
	/**
	 * In case the provided offer is invalid.
	 */
	OFFER_INVALID("The offer is invalid"),
	/**
	 * In case the threshold is invalid.
	 */
	THRESHOLD_INVALID("The threshold value is invalid."),
	/**
	 * In case the provided offer id and msisdn already exist.
	 */
	ALREADY_EXIST("The msisdn, offer id combination already exists"),
	/**
	 * In case the upgrade type is invalid.
	 */
	UPGRADE_TYPE_INVALID("The upgrade type is invalid."),
	/**
	 * In case the consumer limit old is invalid.
	 */
	CONSUMER_LIMIT_OLD_INVALID("The consumer limit old is invalid."),
	/**
	 * In case the expiry date is invalid.
	 */
	EXPIRY_DATE_INVALID("The expiry date is invalid."),
	/**
	 * In case the consumer limit is invalid.
	 */
	CONSUMER_LIMIT_INVALID("Consumer limt is invalid");

	/**
	 * The error message for the error code.
	 */
	private String errorMessage;

	/**
	 * The constructor for error code.
	 * 
	 * @param errorMessage
	 *            the error message to set.
	 */
	private ImportErrorCode(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * This method is used to get the error message.
	 * 
	 * @return the error message.
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
