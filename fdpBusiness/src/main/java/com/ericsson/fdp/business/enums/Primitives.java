package com.ericsson.fdp.business.enums;

import java.util.Date;

/**
 * This class is used to define the primitive types supported. This stores the
 * primitive java object class and the value to be used for the primitive.
 * 
 * @author Ericsson
 * 
 */
public enum Primitives {
	// No longer in use moved to dao.
	/**
	 * The primitive type boolean.
	 */
	BOOLEAN(Boolean.class, "boolean"),
	/**
	 * The primitive type integer.
	 */
	INTEGER(Integer.class, "i4"),
	/**
	 * The primitive type string.
	 */
	STRING(String.class, "string"),
	/**
	 * The primitive type date time.
	 */
	DATETIME(Date.class, "dateTime.iso8601");

	/** The java class corresponding to the primitive type. */
	private Class clazz;
	/** The value of the primitive type. */
	private String value;

	/**
	 * The constructor to create primitive types.
	 * 
	 * @param clazzToSet
	 *            The class of the primitive type.
	 * @param valueToSet
	 *            The value of the primitive type.
	 */
	private Primitives(final Class clazzToSet, final String valueToSet) {
		this.clazz = clazzToSet;
		this.value = valueToSet;
	}

	/**
	 * @return value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * 
	 * @return clazz
	 */
	public Class getClazz() {
		return clazz;
	}

}
