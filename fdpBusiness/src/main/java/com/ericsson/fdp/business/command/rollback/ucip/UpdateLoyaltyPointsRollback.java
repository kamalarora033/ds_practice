package com.ericsson.fdp.business.command.rollback.ucip;

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.ParamTransformationType;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.TransformationUtil;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class is used to implement rollback of UpdateLoyaltyPoints command.
 * 
 * @author Ericsson
 */
public class UpdateLoyaltyPointsRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8825200782881698974L;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(UpdateLoyaltyPointsRollback.class);

	/**
	 * Instantiates a new update fa f list.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public UpdateLoyaltyPointsRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest,
			final Object... otherParams) throws ExecutionFailedException {

		if (otherParams != null && otherParams[0] != null
				&& otherParams[0] instanceof FDPCommand) {

			try {
				final FDPCommand executedCommand = (FDPCommand) otherParams[0];
				List<CommandParam> commandinputparam = executedCommand
						.getInputParam("REQUEST").getChilderen();
				for (int i = 0; i < commandinputparam.size(); i++) {
					CommandParamInput commandParam = (CommandParamInput) commandinputparam
							.get(i);
					if (commandParam.getName().equals("OPERATION_NAME_ATTR")) {
						commandParam.setValue("RollbackRedeemedPoints");
						commandinputparam.set(i, commandParam);
					}

				}
				commandinputparam.add(executedCommand
						.getInputParam("REQUESTDETAILS"));
				this.setInputParam(commandinputparam);

				return executeCommand(fdpRequest);

			} catch (EvaluationFailedException e) {
				throw new ExecutionFailedException("Could not execute command",
						e);
			}

		} else {
			throw new ExecutionFailedException("Could not execute command");
		}
	}
}
