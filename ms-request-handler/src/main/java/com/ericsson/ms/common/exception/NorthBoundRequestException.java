package com.ericsson.ms.common.exception;
/**
 * 
 * @author Ericsson
 *
 */
public class NorthBoundRequestException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * @param message
	 * 			the Message
	 */
	public NorthBoundRequestException(String message) {
		super(message);
	}

}
