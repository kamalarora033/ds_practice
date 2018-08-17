package com.ericsson.fdp.business.charging;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.dao.entity.ExternalSystemType;

/*
Feature Name: DA Based Charging
Changes: Product Charging object added to ChargingConditionStep
Date: 18-12-2015
Singnum Id:ESIASAN
*/

/**
 * This class defines the charging condition step. The condition can be
 * evaluated.
 * 
 * @author Ericsson
 * 
 */
public class ChargingConditionStep implements FDPStep {

	/**
	 * 
	 */
	private static final long serialVersionUID = 610066107922249282L;

	/**
	 * The step id.
	 */
	private Long stepId;
	
	/**
	 * The step name.
	 */
	private String stepName;

	/**
	 * The expression to be evaluated.
	 */
	private Expression expression;

	/**
	 * The charging value to be used.
	 */
	private FDPChargingSystem<? extends Object> chargingType;
	
	/**
	 * The associated product charging
	 */
	private List<ProductCharging> productChargings;
	
	/**
	 * The default charging
	 */
	private ExternalSystem defaultCharging;
	
	/** The Key **/
	private String key;

	private boolean executed;
	
	/**
	 * This method is used to get the step id.
	 * 
	 * @return the step id.
	 */
	public Long getStepId() {
		return stepId;
	}

	@Override
	public String getStepName() {
		return stepName;
	}
	
	/**
	 * @return the charging type.
	 */
	public FDPChargingSystem<? extends Object> getChargingType() {
		return chargingType;
	}
	
	public List<ProductCharging> getProductChargings(){
		if(null == productChargings){
			productChargings = new ArrayList<ProductCharging>();
		}
		return this.productChargings;
	}
	
	/**
	 * Instantiates a new charging condition step.
	 * 
	 * @param expression
	 *            the expression
	 * @param chargingType
	 *            the charging type
	 * @param stepId
	 *            the step id
	 * @param stepName
	 *            the step name
	 */
	public ChargingConditionStep(final Expression expression, final FDPChargingSystem<? extends Object> chargingType,
			final Long stepId, final String stepName) {
		this.expression = expression;
		this.chargingType = chargingType;
		this.stepId = stepId;
		this.stepName = stepName;
	}
	
	/**
	 * Instantiates a new charging condition step.
	 * 
	 * @param expression
	 * @param productCharging
	 * @param stepId
	 * @param stepName
	 */
	public ChargingConditionStep(final Expression expression, final List<ProductCharging> productChargings,
			final Long stepId, final String stepName, final String key) {
		this.expression = expression;
		this.productChargings = productChargings;
		this.stepId = stepId;
		this.stepName = stepName;
		this.key = key;
	}

	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		try {
			return RequestUtil.createStepResponse(expression.evaluateExpression(fdpRequest));
		} catch (ExpressionFailedException e) {
			throw new StepException("The expression could not be evaluated.");
		}
	}

	@Override
	public String toString() {
		return "  charging condition step. The expression formed is :- " + expression + " charging type is :- "
				+ chargingType;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the defaultCharging
	 */
	public ExternalSystem getDefaultCharging() {
		return defaultCharging;
	}

	/**
	 * @param defaultCharging the defaultCharging to set
	 */
	public void setDefaultCharging(ExternalSystem defaultCharging) {
		this.defaultCharging = defaultCharging;
	}
	
	@Override
	public boolean isStepExecuted() {
		return executed;
	}


	@Override
	public void setStepExecuted(boolean stepexecuted) {
		this.executed=stepexecuted;
	}

	
}
