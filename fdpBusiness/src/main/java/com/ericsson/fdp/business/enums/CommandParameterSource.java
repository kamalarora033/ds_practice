package com.ericsson.fdp.business.enums;

/**
 * The enum defines the source of the parameter.
 * 
 * @author Ericsson
 * 
 */
public enum CommandParameterSource {

	/**
	 * In case the parameter source is of type input.
	 */
	INPUT,
	/**
	 * In case the parameter has the source request.
	 */
	REQUEST,
	/**
	 * In case the parameter has the source as command output.
	 */
	COMMAND_OUTPUT,

	/**
	 * In case the parameter is of type function.
	 */
	FUNCTION,
	/**
	 * In case the parameter has source product.
	 */
	PRODUCT,
	/**
	 * In case the parameter is globally set.
	 */
	GLOBAL
}
