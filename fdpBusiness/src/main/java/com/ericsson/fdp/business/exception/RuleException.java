package com.ericsson.fdp.business.exception;

/**
 * The exception when a rule fails.
 * 
 * @author Ericsson
 * 
 */
public class RuleException extends Exception {

	/**
	 * The constructor.
	 * 
	 * @param messageToSet
	 *            The detail message.
	 */
	public RuleException(final String messageToSet) {
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
	public RuleException(final String messageToSet, final Exception causeToSet) {
		super(messageToSet, causeToSet);
	}

	/** The serial version for the class. */
	private static final long serialVersionUID = 1L;

}
