package com.ericsson.fdp.business.command.rollback.rs;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.impl.RecurringCharging;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.dao.enums.ChargingType;

/**
 * The Class RSdeprovisionRollback.
 */
public class RSdeprovisionRollback extends NonTransactionCommand {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 3543183249619369135L;

	/** The Constant USER_NAME. */
	private static final String USER_NAME = "userName";

	/** The Constant PASSWORD. */
	private static final String PASSWORD = "password";

	/**
	 * Instantiates a new rsdeprovision rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 */
	public RSdeprovisionRollback(final String commandDisplayName) {
		super(commandDisplayName);
	}

	@Override
	public Status execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		ChargingValue chargingValue = getChargingValue(fdpRequest, otherParams);
		if (chargingValue != null && fdpRequest instanceof FDPRequestImpl) {
			FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.addMetaValue(RequestMetaValuesKey.CHARGING_STEP, chargingValue);
		}

		try {
			for (final CommandParam commandParam : getInputParam()) {
				if (commandParam instanceof CommandParamInput) {
					final CommandParamInput input = (CommandParamInput) commandParam;
					if (commandParam.getName().equals(USER_NAME)) {
						input.setValue(getParamValue(USER_NAME, otherParams));
					} else if (commandParam.getName().equals(PASSWORD)) {
						input.setValue(getParamValue(PASSWORD, otherParams));
					} else {
						input.evaluateValue(fdpRequest);
					}
				}
			}
			// Generate XML file for the parameter and append it to xml.
			return executeCommand(fdpRequest);
		} catch (final EvaluationFailedException e) {
			throw new ExecutionFailedException("Could not execute command", e);
		}

	}

	/**
	 * Gets the param value.
	 * 
	 * @param param
	 *            the param
	 * @param otherParams
	 *            the other params
	 * @return the param value
	 */
	private Object getParamValue(String param, Object[] otherParams) {
		Object username = null;
		if (otherParams != null & otherParams.length > 0 & otherParams[0] instanceof FDPCacheable) {
			FDPCacheable fdpCacheable = (FDPCacheable) otherParams[0];
			if (fdpCacheable instanceof FDPCommand) {
				FDPCommand fdpCommand = (FDPCommand) fdpCacheable;
				username = fdpCommand.getInputParam(param).getValue();
			}
		}
		return username;
	}

	/**
	 * Gets the charging value.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param otherParams
	 *            the other params
	 * @return the charging value
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private ChargingValue getChargingValue(FDPRequest fdpRequest, Object[] otherParams) throws ExecutionFailedException {
		ChargingValue chargingValue = null;
		FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			Product product = (Product) fdpCacheable;
			List<ProductCharging> productCharging = product.getProductCharging(fdpRequest.getChannel(),
					ChargingType.RS_DEPROVISIONING);
			for (ProductCharging productChar : productCharging) {
				if (productChar instanceof RecurringCharging) {
					chargingValue = productChar.evaluateParameterValue(fdpRequest);
				}
			}
		}
		return chargingValue;
	}
}
