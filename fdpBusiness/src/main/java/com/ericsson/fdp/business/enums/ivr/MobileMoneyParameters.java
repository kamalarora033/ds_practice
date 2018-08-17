package com.ericsson.fdp.business.enums.ivr;

public enum MobileMoneyParameters {

	
	REQUEST_ID("requestID"),
	MSISDN("msisdn")
	;
	
	String value;
	
	private MobileMoneyParameters(String value)
	{
		this.value=value;
	}

	public String getValue() {
		return value;
	}


	
}
