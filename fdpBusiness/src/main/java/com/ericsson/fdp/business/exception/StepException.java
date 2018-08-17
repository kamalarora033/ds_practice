package com.ericsson.fdp.business.exception;

/**
 * The exception when a step fails.
 * 
 * @author Ericsson
 * 
 */
public class StepException extends Exception {

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
	public StepException(final String messageToSet, final Exception causeToSet) {
		super(messageToSet, causeToSet);
	}

	/**
	 * The constructor.
	 * 
	 * @param messageToSet
	 *            The detail message.
	 */
	public StepException(final String messageToSet) {
		super(messageToSet);
	}

}
