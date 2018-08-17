package com.ericsson.fdp.business.charging.impl;

import java.util.List;

import com.ericsson.fdp.business.charging.ProductCharging;
import com.ericsson.fdp.business.exception.StepException;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPStepResponse;
import com.ericsson.fdp.dao.enums.ChargingType;

/**
 * This class implements the other charging steps. This class provides the
 * implementation to execute product charging.
 * 
 * @author Ericsson
 * 
 */
public class OtherChargingStepImpl extends ProductChargingStepImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1012858540532677912L;
	private List<ProductCharging> productChargingSteps = null;

	/**
	 * Instantiates a new other charging step impl.
	 * 
	 * @param productChargingSteps
	 *            the product charging steps
	 */
	public OtherChargingStepImpl(final List<ProductCharging> productChargingSteps, final Long stepId,
			final String stepName, final ChargingType chargingType) {
		super(stepId, stepName, chargingType);
		this.productChargingSteps = productChargingSteps;
	}

	@Override
	public FDPStepResponse executeStep(final FDPRequest fdpRequest) throws StepException {
		FDPStepResponse fdpStepResponse = null;
		if (productChargingSteps != null) {
			fdpStepResponse = super.executeStep(fdpRequest, productChargingSteps);
		}
		return (fdpStepResponse == null) ? RequestUtil.createStepResponse(true) : fdpStepResponse;
	}

	@Override
	public String toString() {
		return " other product charging step impl.";
	}

}
