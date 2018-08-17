package com.ericsson.fdp.business.policy.policyrule.validators;

import java.util.Map;

import com.ericsson.fdp.core.exception.EvaluationFailedException;

/**
 * This interface provides the rule validation. This interface should be
 * implemented by all classes providing custom validation of rule.
 * 
 * @author Ericsson
 * 
 */
public interface RuleValidator {

	/**
	 * This method is used to validate the inputs.
	 * 
	 * @param from
	 *            the from to be used.
	 * @param to
	 *            the to which is to be used.
	 * @return true, if validation succeeds, false otherwise.
	 * @throws EvaluationFailedException
	 *             Exception in evaluation.
	 */
	boolean validate(Map<Object, Object> from, Map<Object, Object> to) throws EvaluationFailedException;


}
