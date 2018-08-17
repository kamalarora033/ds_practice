package com.ericsson.fdp.business.policy.policyrule.impl;

import com.ericsson.fdp.business.enums.PolicyValidationMessageEnum;
import com.ericsson.fdp.common.constants.FDPConstant;

public final class PolicyRuleValidateImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	public static boolean isNullorEmpty(Object o) {
		return (o!= null && !o.toString().isEmpty());
	}

	
	public static boolean isInteger(Object o) {
		if(!isNullorEmpty(o))
			return false;
		try{
			Integer.parseInt(o.toString());
			return true;
		}catch(NumberFormatException e){
			return false;	
		}
	}

	
	public static boolean isDouble(Object o) {
		if(!isNullorEmpty(o))
			return false;
		try{
			Double.parseDouble(o.toString());
			return true;
		}catch(NumberFormatException e){
			return false;	
		}
	}

	
	public static String errorMsg(PolicyValidationMessageEnum pvme, String suggestionMsg) {
		StringBuilder msg = new StringBuilder(pvme.msg());
		if (suggestionMsg != null) {
			msg.append(FDPConstant.DOT + suggestionMsg);
		}
		return msg.toString();
	}

}
