package com.ericsson.fdp.business.operation.impl;

import com.ericsson.fdp.business.operation.Operation;
import com.ericsson.fdp.business.util.OperationUtil;
import com.ericsson.fdp.core.exception.EvaluationFailedException;

public class DivOperation implements Operation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2895870100566520886L;

	/**
	 * The Division Value.
	 */
	private Double divVal;

	/**
	 * The Precision Value.
	 */
	private int precisionValue;

	public DivOperation(Double divVal, int precisionValue) {
		this.divVal = divVal;
		this.precisionValue = precisionValue;
	}

	@Override
	public double evaluate(double input) throws EvaluationFailedException {
		double value = (input / divVal);
		return OperationUtil.convertToPrecision(value, precisionValue);
	}
}
