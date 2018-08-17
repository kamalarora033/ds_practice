package com.ericsson.fdp.business.request;

import java.io.Serializable;

/**
 * The Class ReponseStatus.
 */
public class ResponseStatus implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2087932534340484811L;

	/** The response code. */
	private String responseCode;
	
	/** The response string. */
	private String responseString;
	
	/**
	 * Instantiates a new reponse status.
	 *
	 * @param responseCode the response code
	 * @param responseString the response string
	 */
	public ResponseStatus(String responseCode, String responseString) {
		super();
		this.responseCode = responseCode;
		this.responseString = responseString;
	}
	
	/**
	 * Gets the response code.
	 *
	 * @return the response code
	 */
	public String getResponseCode() {
		return responseCode;
	}
	
	/**
	 * Sets the response code.
	 *
	 * @param responseCode the new response code
	 */
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	
	/**
	 * Gets the response string.
	 *
	 * @return the response string
	 */
	public String getResponseString() {
		return responseString;
	}
	
	/**
	 * Sets the response string.
	 *
	 * @param responseString the new response string
	 */
	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}
	
	
}
