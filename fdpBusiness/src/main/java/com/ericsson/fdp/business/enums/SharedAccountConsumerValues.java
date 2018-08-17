package com.ericsson.fdp.business.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum defines the fields required for shared account consumer values.
 * 
 * @author Ericsson
 * 
 */
public enum SharedAccountConsumerValues {
	/**
	 * The parent msisdn.
	 */
	GroupParentMSISDN("GroupParentMSISDN"),
	/**
	 * The parent offer id.
	 */
	GroupParentOfferId("GroupParentOfferId"),
	/**
	 * The consumer limit.
	 */
	ConsumerLimit("ConsumerLimit"),
	/**
	 * The consumer threshold unit.
	 */
	ConsumerThresholdUnit("ConsumerThresholdUnit"),
	/**
	 * The consumer offer id.
	 */
	ConsumerOfferId("ConsumerOfferId"),
	/**
	 * The consumer msisdn.
	 */
	ConsumerMSISDN("ConsumerMSISDN"),
	/**
	 * The consumer name.
	 */
	ConsumerName("ConsumerName"),
	/**
	 * The upgrade type.
	 */
	UpgradeType("UpgradeType"),
	/**
	 * The consumer limit old.
	 */
	ConsumerLimitOld("ConsumerLimitOld"),
	/**
	 * The upgrade expiry date.
	 */
	UpgradeExpiryDate("UpgradeExpiryDate"),
	/**
	 * The row number.
	 */
	RowNum("rowNum"),
	/**
	 * The error code.
	 */
	ERROR_CODE("errorCode");

	/**
	 * The header value for the enum.
	 */
	private String headerValue = null;

	/**
	 * The constructor.
	 * 
	 * @param headerValue
	 *            the header value to set.
	 */
	private SharedAccountConsumerValues(final String headerValue) {
		this.headerValue = headerValue;
	}

	/**
	 * This method is used to get the list of headers. To enable case
	 * insensitivity, the headers are in lower case.
	 * 
	 * @return the list of headers.
	 */
	public static List<String> getHeaders() {
		List<String> headers = new ArrayList<String>();
		for (SharedAccountConsumerValues sharedAccountConsumerValues : SharedAccountConsumerValues.values()) {
			if (!(SharedAccountConsumerValues.ERROR_CODE.equals(sharedAccountConsumerValues) 
					|| SharedAccountConsumerValues.RowNum.equals(sharedAccountConsumerValues))) {
				headers.add(sharedAccountConsumerValues.headerValue.toLowerCase());
			}
		}
		return headers;
	}

	/**
	 * This method is used to get the header value.
	 * 
	 * @return the header value.
	 */
	public String getHeaderValue() {
		return headerValue.toLowerCase();
	}

}
