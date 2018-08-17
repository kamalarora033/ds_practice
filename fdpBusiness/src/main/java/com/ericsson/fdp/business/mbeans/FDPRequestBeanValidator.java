/**
 * 
 */
package com.ericsson.fdp.business.mbeans;

/**
 * The Class FDPRequestBeanValidator.
 * 
 * @author Ericsson
 */
public class FDPRequestBeanValidator {

	/** The error message. */
	private String message;

	/** The is valid. */
	private Boolean isValid;

	public String getMessage() {
		return message;
	}

	public void setMessage(String errorMessage) {
		this.message = errorMessage;
	}

	public Boolean getIsValid() {
		return isValid;
	}

	public void setIsValid(Boolean isValid) {
		this.isValid = isValid;
	}

}
