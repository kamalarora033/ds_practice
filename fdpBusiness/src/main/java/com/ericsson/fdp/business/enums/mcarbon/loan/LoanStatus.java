package com.ericsson.fdp.business.enums.mcarbon.loan;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * This Enum represents the Loan Eligibility Status of Subscriber.
 * @author evarrao
 *
 */

@XmlType
@XmlEnum(String.class)
public enum LoanStatus {
	
	@XmlEnumValue("0")
	ELIGIBLE("0"),
	
	@XmlEnumValue("1")
	NOT_ELIGIBLE("1");
	
	
	
	private String value;
	
	private LoanStatus(String value){
		this.value = value;
	}
	
	public String getValue(){
		return this.value;
	}
}
