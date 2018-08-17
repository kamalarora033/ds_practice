package com.ericsson.fdp.business.policy.policyrule.validators;

import java.util.Map;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.exception.EvaluationFailedException;

/**
 * This class implements the circle validation for the inputs.
 * 
 * @author Ericsson
 * 
 */
public class ConfirmationValidator implements RuleValidator {

	@Override
	public boolean validate(final Map<Object, Object> from, final Map<Object, Object> to)
			throws EvaluationFailedException {
		Object confirmation = from.get(FDPConstant.CONFIRMATION_KEY);
		boolean valid = false;
		if ("0".equals(confirmation.toString())) {
			valid = true;
		}
		return valid;
	}

}
