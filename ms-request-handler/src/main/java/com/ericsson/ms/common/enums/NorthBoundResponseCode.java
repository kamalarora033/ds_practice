package com.ericsson.ms.common.enums;

/**
 * The NorthBoundResponseCode Enum
 * 
 * @author Ericsson
 *
 */
public enum NorthBoundResponseCode {

	/** The INVALID_PARAMETER constant */
	INVALID_PARAMETER("Invalid Request parameter", 401),
	/** The SUCCESS constant */
	SUCCESS("success", 200),
	/** The Failure Constant */
	FAILURE("failure",400);

	/** The response description */
	private String responseDesc;

	/** The response code */
	private int responseCode;

	/**
	 * @param responseDesc
	 * @param responseCode
	 */
	private NorthBoundResponseCode(String responseDesc, int responseCode) {
		this.responseDesc = responseDesc;
		this.responseCode = responseCode;
	}

	/**
	 * @return the responseDesc
	 */
	public String getResponseDesc() {
		return responseDesc;
	}

	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

}
