package com.ericsson.fdp.business.exception;

/**
 * The exception when a expression fails.
 * 
 * @author Ericsson
 * 
 */
public class ExpressionFailedException extends Exception {

	/** The serial version for the class. */
	private static final long serialVersionUID = 1L;

	/**
	 * The constructor.
	 * 
	 * @param messageToSet
	 *            The detail message.
	 */
	public ExpressionFailedException(final String messageToSet) {
		super(messageToSet);
	}

	/**
	 * The constructor.
	 * 
	 * @param messageToSet
	 *            The detail message.
	 * @param causeToSet
	 *            The cause of the exception.
	 */
	public ExpressionFailedException(final String messageToSet, final Exception causeToSet) {
		super(messageToSet, causeToSet);
	}

}
