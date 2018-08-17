package com.ericsson.fdp.business.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * This enum defines the fields required for shared account provider values.
 * 
 * @author Ericsson
 * 
 */
public enum SharedAccountProviderValues {
	/**
	 * The parent msisdn.
	 */
	GroupParentMSISDN("GroupParentMSISDN"),
	/**
	 * The parent offer id.
	 */
	GroupParentOfferId("GroupParentOfferId"),
	/**
	 * The group name.
	 */
	GroupName("GroupName"),
	/**
	 * The group provider name.
	 */
	GroupProviderName("GroupProviderName"),
	/**
	 * The group provider msisdn.
	 */
	GroupMSISDN("GroupMSISDN"),
	/**
	 * The provider threshold unit.
	 */
	ProviderThresholdUnit("ProviderThresholdUnit"),
	/**
	 * The group offer id.
	 */
	GroupOfferId("GroupOfferId"),
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
	private SharedAccountProviderValues(final String headerValue) {
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
		for (SharedAccountProviderValues sharedAccountProviderValues : SharedAccountProviderValues.values()) {
			if (!(SharedAccountProviderValues.ERROR_CODE.equals(sharedAccountProviderValues) 
					|| SharedAccountProviderValues.RowNum
					.equals(sharedAccountProviderValues))) {
				headers.add(sharedAccountProviderValues.headerValue.toLowerCase());
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
