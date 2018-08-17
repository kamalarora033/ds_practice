package com.ericsson.fdp.business.util;

import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.core.exception.EvaluationFailedException;

/**
 * This class is used to provide utility methods for class conversions.
 */
public final class ClassUtil {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);

	/**
	 * Instantiates a new class util.
	 */
	private ClassUtil() {
	}

	/**
	 * This method is used to get the primitive value of the class provided.
	 * 
	 * @param outputVariable
	 *            The variable which is to be cast.
	 * @param clazz
	 *            The class to be casted into.
	 * @return The casted object.
	 * @throws EvaluationFailedException
	 *             If, casting fails.
	 */
	public static Object getPrimitiveValue(Object outputVariable, Class<?> clazz) throws EvaluationFailedException {
		Object value = null;
		if (Boolean.class.equals(clazz)) {
			if (outputVariable.toString().equalsIgnoreCase(Boolean.TRUE.toString())) {
				value = Boolean.TRUE;
			} else if (outputVariable.toString().equalsIgnoreCase(Boolean.FALSE.toString())) {
				value = Boolean.FALSE;
			} else {
				try {
					value = Integer.parseInt(outputVariable.toString()) == 0 ? Boolean.FALSE : Boolean.TRUE;
				} catch (NumberFormatException e) {
					LOGGER.error("Exception Occured.", e);
					value = null;
				}
			}
		} else if (String.class.equals(clazz)) {
			value = outputVariable instanceof List? outputVariable: outputVariable.toString();
			/*if (!(outputVariable instanceof ArrayList))
				value = outputVariable.toString();
			else
				 value=outputVariable;*/
		} else if (Date.class.equals(clazz)) {
			value = outputVariable instanceof List ? outputVariable : getValueForDate(outputVariable);
		} else if (Integer.class.equals(clazz)) {
			try {
				if(!(outputVariable instanceof List)){
					value = Integer.parseInt(outputVariable.toString());
				}
			} catch (NumberFormatException e) {
				value = null;
			}
		} else if (BigInteger.class.equals(clazz)) {
			try {
				value = new BigInteger(outputVariable.toString());
			} catch (NumberFormatException e) {
				LOGGER.error("Exception Occured.", e);
				value = null;
			}
		} else if (Long.class.equals(clazz)) {
			try {
				value = Long.valueOf(outputVariable.toString());
			} catch (NumberFormatException e) {
				LOGGER.error("Exception Occured.", e);
				value = null;
			}
		}
		return value;
	}

	private static Object getValueForDate(Object outputVariable) throws EvaluationFailedException {
		Object value = null;
		try {
			value = (outputVariable instanceof GregorianCalendar) ? outputVariable : DateUtil
					.getDateTimeFromFDPDateTimeFormat(outputVariable.toString());
		} catch (ParseException e) {
			try {
				value = DateUtil.getDateFromFDPDatFormat(outputVariable.toString());
			} catch (ParseException e2) {
				throw new EvaluationFailedException("The date time format is not valid.", e2);
			}
		}
		return value;
	}

	/**
	 * This method is used to get the primitive value of the class provided. It
	 * does not return null values insted returns the value provided if casting
	 * fails.
	 * 
	 * @param outputVariable
	 *            The variable which is to be cast.
	 * @param clazz
	 *            The class to be casted into.
	 * @return The casted object.
	 * @throws EvaluationFailedException
	 *             If, casting fails.
	 */
	public static Object getPrimitiveValueReturnNotNullObject(Object outputVariable, Class<?> clazz)
			throws EvaluationFailedException {
		Object primitiveVal = getPrimitiveValue(outputVariable, clazz);
		return primitiveVal != null ? primitiveVal : outputVariable;
	}

	/**
	 * The long value.
	 * 
	 * @param retryNumber
	 *            the retry number.
	 * @return the retry value.
	 */
	public static Long getLongValue(final String retryNumber) {
		Long retryNum = null;
		try {
			retryNum = Long.valueOf(retryNumber);
		} catch (NumberFormatException e) {
			retryNum = null;
		}
		return retryNum;
	}
}
