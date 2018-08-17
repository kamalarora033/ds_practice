package com.ericsson.fdp.business.command.param.impl;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.impl.TransactionCommand;
import com.ericsson.fdp.business.command.param.AbstractCommandParam;

/**
 * This class defines the output parameter for the commands.
 * 
 * @author Ericsson
 * 
 */
public class CommandParamOutput extends AbstractCommandParam {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7663586514233902796L;

	/**
	 * The command param output constructor.
	 */
	public CommandParamOutput() {
		super();
	}

	/**
	 * The command param output constructor.
	 * 
	 * @param commandDisplayName
	 *            The command display name.
	 * @param fullyQualifiedPath
	 *            The fully qualified path of the param.
	 * @param isTransactional
	 *            true, if the command associated is transactional.
	 */
	public CommandParamOutput(final String commandDisplayName, final String fullyQualifiedPath,
			final boolean isTransactional) {
		if (isTransactional) {
			super.setCommand(new TransactionCommand(commandDisplayName));
		} else {
			super.setCommand(new NonTransactionCommand(commandDisplayName));
		}
		super.setFullyQualifiedPathOfParameter(fullyQualifiedPath);
	}

	/**
	 * The command param output constructor.
	 * 
	 * @param commandDisplayName
	 *            The command display name.
	 * @param fullyQualifiedPath
	 *            The fully qualified path of the param.
	 */
	public CommandParamOutput(final String commandDisplayName, final String fullyQualifiedPath) {
		this(commandDisplayName, fullyQualifiedPath, false);
	}
}
