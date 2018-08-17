package com.ericsson.ms.common.enums;

/**
 * @author GUR51924
 * 
 */
public enum ActionEnum {

	/**
	 * these are the action types
	 */

	PRE_EXPIRY_ADHOC("PRE_EXPIRY_ADHOC"), PRE_EXPIRY_RENEWAL("PRE_EXPIRY_RENEWAL"), POST_EXPIRY_ADHOC("POST_EXPIRY_ADHOC"), POST_EXPIRY_RENEWAL("POST_EXPIRY_RENEWAL");
	/**
	 * param name used to access name of the parameter
	 */
	private String paramName;

	private ActionEnum(String paramName) {
		this.paramName = paramName;
	}

	/**
	 * @return
	 */
	public String getParamName() {
		return paramName;
	}

}
