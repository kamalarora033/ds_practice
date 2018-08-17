package com.ericsson.fdp.business.exception;

/**
 * The exception when a notification fails.
 * 
 * @author Ericsson
 * 
 */
public class NotificationFailedException extends Exception {

	/** The serial version for the class. */
	private static final long serialVersionUID = 1L;

	/**
	 * The constructor.
	 * 
	 * @param messageToSet
	 *            The detail message.
	 * @param causeToSet
	 *            The cause of the exception.
	 */
	public NotificationFailedException(final String messageToSet, final Exception causeToSet) {
		super(messageToSet, causeToSet);
	}


}
