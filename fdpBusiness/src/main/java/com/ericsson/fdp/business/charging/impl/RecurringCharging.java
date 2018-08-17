package com.ericsson.fdp.business.charging.impl;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.charging.AbstractCharging;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This class defines the values to be used in case of recurring charging.
 * 
 * @author Ericsson
 * 
 */
public class RecurringCharging extends AbstractCharging {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6976245079555121040L;
	/**
	 * The rs parameters to be used.
	 */
	private final String rsParameters;
	
	private final String rsCharginAmt;

	private ChargingValueImpl chargingValueImpl;

	public RecurringCharging(final String rsParameters, final String commandDisplayName ,final String rsCharginAmt) {
		this.rsParameters = rsParameters;
		this.rsCharginAmt = rsCharginAmt;
		super.setCommandDisplayName(commandDisplayName);
	}
	
	public RecurringCharging(final String rsParameters, final String commandDisplayName ,final String rsCharginAmt, final ExternalSystem externalSystem) {
		this(rsParameters, commandDisplayName, rsCharginAmt);
		this.externalSystem = externalSystem;
	}

	@Override
	public ChargingValue evaluateParameterValue(final FDPRequest fdpRequest) throws ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		if (chargingValueImpl == null) {
			chargingValueImpl = new ChargingValueImpl();
			chargingValueImpl.setChargingValue(rsParameters);
			chargingValueImpl.setChargingAmount(rsCharginAmt);
			FDPLogger.debug(circleLogger, getClass(), "evaluateParameterValue()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Evaluating recurring charging parameter value :- "
							+ rsParameters);
			chargingValueImpl.setChargingRequired(true);
		}
		return chargingValueImpl;
	}

	/**
	 * @return the rsParameters
	 */
	public String getRsParameters() {
		return rsParameters;
	}

	/**
	 * @return the rsCharginAmt
	 */
	public String getRsCharginAmt() {
		return rsCharginAmt;
	}
}
