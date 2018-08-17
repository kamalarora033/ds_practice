package com.ericsson.fdp.business.exception;

/**
 * The exception when a rollback fails.
 * 
 * @author Ericsson
 * 
 */
public class RollbackException extends Exception {

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
	public RollbackException(final String messageToSet, final Exception causeToSet) {
		super(messageToSet, causeToSet);
	}

	/**
	 * The constructor.
	 * 
	 * @param messageToSet
	 *            The detail message.
	 */
	public RollbackException(final String messageToSet) {
		super(messageToSet);
	}

}
