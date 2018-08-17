package com.ericsson.fdp.business.operation.impl;

import com.ericsson.fdp.business.operation.Operation;
import com.ericsson.fdp.core.exception.EvaluationFailedException;

public class ModOperation implements Operation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2895870100566520886L;

	/**
	 * The Modulo Value.
	 */
	private Long moduloVal;
	
	public ModOperation(Long moduloVal) {
		this.moduloVal = moduloVal;
	}

	@Override
	public double evaluate(double input) throws EvaluationFailedException {
		return (input % moduloVal);

	}

}
