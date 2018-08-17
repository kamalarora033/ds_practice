package com.ericsson.fdp.business.dto;

import java.io.Serializable;

import com.ericsson.fdp.business.enums.ResponseType;

/**
 * The Class ResponseDTO.
 */
public class ResponseDTO implements Serializable{

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -36868962401627775L;
	
	/** The response type. */
	private ResponseType responseType;
	
	/** The message. */
	private String message;
	
	public ResponseDTO(ResponseType responseType, String message) {
		this.responseType =responseType;
		this.message = message;
	}

	/**
	 * Gets the response type.
	 *
	 * @return the responseType
	 */
	public ResponseType getResponseType() {
		return responseType;
	}

	/**
	 * Sets the response type.
	 *
	 * @param responseType the responseType to set
	 */
	public void setResponseType(ResponseType responseType) {
		this.responseType = responseType;
	}

	/**
	 * Gets the message.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the message.
	 *
	 * @param message the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ResponseDTO [responseType=" + responseType + ", message="
				+ message + "]";
	}
	
}
