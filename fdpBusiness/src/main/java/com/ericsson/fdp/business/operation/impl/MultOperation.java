package com.ericsson.fdp.business.operation.impl;

import com.ericsson.fdp.business.operation.Operation;
import com.ericsson.fdp.business.util.OperationUtil;
import com.ericsson.fdp.core.exception.EvaluationFailedException;

public class MultOperation implements Operation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2895870100566520886L;

	/**
	 * The Multiplication Value.
	 */
	private Double multVal;

	/**
	 * The Precision Value.
	 */
	private int precisionValue;
	
	public MultOperation(Double multVal, int precisionValue) {
		this.multVal = multVal;
		this.precisionValue = precisionValue;
	}

	@Override
	public double evaluate(double input) throws EvaluationFailedException {
		double value = (input * multVal);
		return OperationUtil.convertToPrecision(value, precisionValue);
	}

	public static void main(String[] args) {

		double value = (123.123123 / 10);
		double prec = Math.pow(10, 0);
		value = ((int) (value * (prec)) / prec);
		
	}

}
