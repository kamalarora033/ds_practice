package com.ericsson.fdp.business.charging.impl;

import java.util.List;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.business.charging.AbstractCharging;
import com.ericsson.fdp.business.charging.ChargingConditionStep;
import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.exception.RollbackException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.util.ChargingUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ProductUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.vo.CommandExecutionStatus;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;

/*
Feature Name: Variable MAIN + DA Based Charging
Changes: Handled variable MAIN + DA based charging
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * This class defines the values to be used in case of recurring charging.
 * 
 * @author Ericsson
 * 
 */
public class VariableCharging extends AbstractCharging {

	private static final long serialVersionUID = 4306534055434761645L;
	
	/**
	 * The list of charging condition steps. For the step which evaluates to
	 * true the corresponding value will be charged.
	 */
	private List<ChargingConditionStep> conditionStep;

	private ChargingValueImpl chargingValueImpl;
	
	private ProductCharging applicableCharging;
		
	public List<ChargingConditionStep> getConditionStep() {
		return conditionStep;
	}

	public void setConditionStep(final List<ChargingConditionStep> conditionStep) {
		this.conditionStep = conditionStep;
	}

	public ProductCharging getApplicableCharging() {
		return applicableCharging;
	}

	public void setApplicableCharging(ProductCharging applicableCharging) {
		this.applicableCharging = applicableCharging;
	}

	/**
	 * Instantiates a new variable charging
	 * 
	 * @param conditionStep
	 * @param commandDisplayName
	 */
	public VariableCharging(final List<ChargingConditionStep> conditionStep, final String commandDisplayName) {
		this.conditionStep = conditionStep;
		super.setCommandDisplayName(commandDisplayName);
	}
	
	/**
	 * Instantiates a new variable charging
	 * 
	 * @param conditionStep
	 * @param commandDisplayName
	 * @param externalSystem
	 */
	public VariableCharging(final List<ChargingConditionStep> conditionStep, final String commandDisplayName, final ExternalSystem externalSystem) {
		this(conditionStep, commandDisplayName);
		this.externalSystem = externalSystem;
	}
	
	public CommandExecutionStatus execute(final FDPRequest fdpRequest, final Object... otherParams) throws ExecutionFailedException {
		CommandExecutionStatus commandExecutionStatus = new CommandExecutionStatus(Status.SUCCESS, 0, null, null, null);
		applicableCharging = getChargingToExecute(fdpRequest);
		commandExecutionStatus = applicableCharging.execute(fdpRequest);
		return commandExecutionStatus;	
	}
	
	@Override
	public ChargingValue evaluateParameterValue(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (chargingValueImpl == null) {
			chargingValueImpl = new ChargingValueImpl();
			final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
			FDPLogger.debug(circleLogger, getClass(), "evaluateParameterValue()", LoggerUtil.getRequestAppender(fdpRequest) + "Fetching parameter value for variable charging");
			FixedCharging chargingToExecute = (FixedCharging) getChargingToExecute(fdpRequest);
			chargingValueImpl.setChargingValue(chargingToExecute.chargingValue.getChargingValue());
			chargingValueImpl.setContentType(chargingToExecute.chargingValue.getContentType());
			chargingValueImpl.setChargingRequired(chargingToExecute.getChargingValue().getIsChargingRequired());
			chargingValueImpl.setExternalSystemToUse(chargingToExecute.getChargingValue().getChargingExternalSystem());
		}	
		return chargingValueImpl;
	}
	
	/**
	 * This method will return a list of charging that are associated
	 * with the condition step that evaluates to true for the subscriber
	 * 
	 * @return
	 * @throws ExecutionFailedException 
	 */
	public List<ProductCharging> getApplicableChargings(final FDPRequest fdpRequest) throws ExecutionFailedException{
		List<ProductCharging> applicableChargings = null;
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		for (final ChargingConditionStep chargingConditionStep : conditionStep) {
			try {
				if (RequestUtil.checkExecutionStatus(chargingConditionStep.executeStep(fdpRequest))) {
					if(chargingConditionStep.getProductChargings() != null){
						applicableChargings = chargingConditionStep.getProductChargings();
						if(applicableChargings == null || applicableChargings.isEmpty()){
							throw new ExecutionFailedException("Product Charging not found for condition step");
						}
						if(fdpRequest.getExternalSystemToCharge() == null){
							((FDPRequestImpl)fdpRequest).setExternalSystemToCharge(chargingConditionStep.getDefaultCharging());
						}
						break;
					}
				}
			}catch(final StepException e){
				FDPLogger.error(circleLogger, getClass(), "evaluateParameterValue()",
						LoggerUtil.getRequestAppender(fdpRequest) + "The condition could not be evaluated.", e);
				throw new ExecutionFailedException("The condition could not be evaluated.", e);
			}
		}
		if(null == applicableChargings){
			throw new ExecutionFailedException("The condition was not satisfied for subscriber.");
		}
		return applicableChargings;
	}
	
	/**
	 * This method will return the charging as per the charging condition that is satisfied
	 * and the external system to charge
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException 
	 * @throws StepException 
	 */
	public ProductCharging getChargingToExecute(final FDPRequest fdpRequest) throws ExecutionFailedException{
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		ProductCharging chargingToExecute = null;
		try {
			List<ProductCharging> eligibleChargings = ProductUtil.getExecutableFDPProductCharging(ChargingUtil.getEligibleChargings(
									getApplicableChargings(fdpRequest), fdpRequest, circleLogger));
			chargingToExecute = eligibleChargings.get(0);
		} catch (final StepException se) {
			throw new ExecutionFailedException(se.getMessage(), se);
		}
		return chargingToExecute;
	}
	
	@Override
	public boolean performRollback(final FDPRequest fdpRequest) throws RollbackException {
		boolean rollbackDone = false;
		try {
			if (applicableCharging == null) {
				applicableCharging = getChargingToExecute(fdpRequest);
			}
			applicableCharging.performRollback(fdpRequest);
		} catch (final ExecutionFailedException e) {
			throw new RollbackException("Could not find command to update", e);
		}
		return rollbackDone;
	}
	
}
