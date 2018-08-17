package com.ericsson.fdp.business.enums;

/**
 * This class defines the condition types that are possible.
 * 
 * @author Ericsson
 * 
 */
public enum ConditionType {
	// TODO: Remove this enum as it is no longer used.
	// PLEASE NOTE :- DO NOT USE THIS ENUM. USE FDPCommandParameterConditionEnum
	// INSTEAD.
	/** If the condition is of type range. */
	RANGE, CONTAINS,
	/** If the condition is of type equals. */
	EQUALS,
	/** If the condition is of type greater than. */
	GREATER_THAN,
	/** If the condition is of type greater than or equals. */
	GREATER_THAN_OR_EQUALS,
	/** If the condition is of type less than. */
	LESSER_THAN,
	/** If the condition is of type less than or equals. */
		LESSER_THAN_OR_EQUALS,
	/** If the condition is NOT  equals to */
	NOT_EQUALS_TO
	;
}
