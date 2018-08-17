package com.ericsson.fdp.business.command.rollback.evds;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of updateBalanceAndDate command.
 * 
 * @author Ericsson
 */
public class EVDSWalletAdjustmentRollback extends NonTransactionCommand {

	private static final long serialVersionUID = 7731131873200815605L;
	
	private static final String TRANS_TYPE_ID = "transtypeid";
	private static final String CREDIT_TRANS_TYPE_ID = "112";

	/**
	 * Instantiates a new update balance and date rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public EVDSWalletAdjustmentRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	/**
	 * This method will change the TRANS_TYPE from type debit(113) to
	 * credit(112) and execute the wallet adjustment command
	 */
	@Override
	public Status execute(final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {
		if (otherParams != null && otherParams[0] != null && otherParams[0] instanceof FDPCommand) {
			try {
				final FDPCommand executedCommand = (FDPCommand) otherParams[0];
				if (executedCommand.getInputParam(TRANS_TYPE_ID) != null) {
					final CommandParamInput commandParamInput = (CommandParamInput) executedCommand.getInputParam(TRANS_TYPE_ID);
					commandParamInput.setValue(CREDIT_TRANS_TYPE_ID);
					this.setInputParam(executedCommand.getInputParam());
				}
				return executeCommand(fdpRequest);
			} catch (final EvaluationFailedException e) {
				throw new ExecutionFailedException("Could not execute command", e);
			}
		} else {
			throw new ExecutionFailedException("Could not find executed command");
		}
	}
}
