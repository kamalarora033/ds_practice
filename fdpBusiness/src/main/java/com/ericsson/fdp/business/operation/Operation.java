package com.ericsson.fdp.business.operation;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.core.exception.EvaluationFailedException;

/**
 * This interface defines the operations.
 * 
 * @author Ericsson
 * 
 */
public interface Operation extends FDPCacheable {

	double evaluate(double input) throws EvaluationFailedException;

}
