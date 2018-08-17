package com.ericsson.fdp.business.enums.mcarbon.loan;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * This Enum represents the status of credit done after get loan action.
 * @author evarrao
 *
 */
@XmlType
@XmlEnum(String.class)
public enum CreditStatus {
	
	@XmlEnumValue("20")
	SUCCESS("20"),
	
	@XmlEnumValue("21")
	FAILURE("21");
	
	
	private String statusValue;
	
	private CreditStatus(String statusValue){
		this.statusValue = statusValue;
	}
	
	public String getSatusValue(){
		return this.statusValue;
	}
}
