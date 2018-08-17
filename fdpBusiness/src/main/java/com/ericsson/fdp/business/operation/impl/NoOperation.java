package com.ericsson.fdp.business.operation.impl;

import com.ericsson.fdp.business.operation.Operation;
import com.ericsson.fdp.core.exception.EvaluationFailedException;

public class NoOperation implements Operation {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2995175623955074130L;

	@Override
	public double evaluate(double input) throws EvaluationFailedException {
		return input;
	}

}
