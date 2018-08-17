package com.ericsson.fdp.business.enums;
/**
 * This Enum is for the Policy Validation Messages 
*/
public enum PolicyValidationMessageEnum {

	/** Input is Invalid/Incorrect **/
	ERROR_MSG("Input is Invalid/Incorrect"),
	/** Kindly provide some input.**/
	SUGGESTION_MSG("Kindly provide some input."),
	/** The policy rule could not be evaluated. **/
	
	INVALID_MSISDN_FORMAT("Enter Msisdn format is not valid"),
	
	MSISDN_IS_NOT_ONNET("Offnet Msisdn is not allowed"),
	
	MAGIC_CANNOT_SAME_AS_MSISDN("Subscriber number cannot added as magic No"),
	
	EXCEPTION_MSG("The policy rule could not be evaluated."),
	
	CANCEL_NOTIFICATION("Purchase cancelled. Please Dial *138# for all your Bundles on MTN");

	
	/** The msg **/
	private String msg;
	
	/**
	 * Instantiates a Policy Validation msg.
	 *
	 * @param name the index
	 */
	PolicyValidationMessageEnum(String msg) {
		this.msg = msg;
	}

	/**
	 * Gets the msg.
	 *
	 * @return the msg
	 */
	public String msg() {
		return msg;
	}

}
