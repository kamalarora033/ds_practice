package com.ericsson.fdp.business.util;

import com.ericsson.fdp.business.enums.ParamTransformationType;

/**
 * This class provides utility functions for transformations.
 * 
 * @author Ericsson
 */
public class TransformationUtil {

	/**
	 * Instantiates a new transformation util.
	 */
	private TransformationUtil() {

	}

	/**
	 * This method evaluates the transformation on the parameter if required.
	 * 
	 * @param parameterValue
	 *            The value to be transformed.
	 * @param paramTransformationType
	 *            the param transformation to be performed.
	 * @return The transformed value.
	 */
	public static Object evaluateTransformation(final Object parameterValue,
			final ParamTransformationType paramTransformationType) {
		Object paramValue = parameterValue;
		switch (paramTransformationType) {
		case NONE:
			break;
		case NEGATIVE:
			if (parameterValue instanceof Double) {
				paramValue = (-1 * (Double) parameterValue);
			} else if (parameterValue instanceof Integer) {
				paramValue = (-1 * (Integer) parameterValue);
			} else if (parameterValue instanceof Float) {
				paramValue = (-1 * (Float) parameterValue);
			} else if (parameterValue instanceof Long) {
				paramValue = (-1 * (Long) parameterValue);
			}
		default:
			break;
		}
		return paramValue;
	}

}
