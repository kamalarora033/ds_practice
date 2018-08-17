package com.ericsson.fdp.business.expression;

import java.io.Serializable;

import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * The fdp expression that can be evaluated.
 * 
 * @author Ericsson
 * 
 */
public interface Expression extends Serializable {

	/**
	 * This method evaluates the expression.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @return True, if the expression is satisfied, false otherwise.
	 * @throws ExpressionFailedException
	 *             Exception, if the expression could not be evaluated.
	 */
	boolean evaluateExpression(FDPRequest fdpRequest) throws ExpressionFailedException;

}
