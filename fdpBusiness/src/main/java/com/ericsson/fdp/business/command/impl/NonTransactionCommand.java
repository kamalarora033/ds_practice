package com.ericsson.fdp.business.command.impl;

import com.ericsson.fdp.business.command.AbstractCommand;

/**
 * This class implements the non-transactional commands such as GET commands.
 * 
 * @author Ericsson.
 * 
 */
public class NonTransactionCommand extends AbstractCommand {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4002287324314947446L;

	/**
	 * The constructor for non transaction command.
	 * 
	 * @param commandDisplayName
	 *            the command display name.
	 */
	public NonTransactionCommand(final String commandDisplayName) {
		super.setCommandDisplayName(commandDisplayName);
	}

	/**
	 * The constructor for non transaction command.
	 */
	public NonTransactionCommand() {
		super();
	}
	
}
