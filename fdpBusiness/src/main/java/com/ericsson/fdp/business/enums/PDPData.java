package com.ericsson.fdp.business.enums;

import java.util.regex.Pattern;

/**
 * This enum defines the PDP data.
 * 
 * @author Ericsson
 * 
 */
public enum PDPData {

	APNID("APNID", Pattern.compile("[0-9]+")), 
	PDPADD("PDPADD", Pattern.compile("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$")), 
	EQOSID("PDPADD", Pattern.compile("[0-9]+")), 
	
	VPAA("VPAA", Pattern.compile("(NO)+|(YES)+")), 
	
	PDPCH("PDPCH", Pattern
			.compile("[0-1]{1}[0-9]{1}[-,0-9]*")), 
			
	PDPTY("PDPTY", Pattern.compile("(IPV4)+|(IPV6)+")), 
	PDPID("PDPID", Pattern.compile("[0-9]+"));

	/**
	 * The pattern.
	 */
	private Pattern pdpPattern;

	/**
	 * The name.
	 */
	private String pdpName;

	private PDPData(String pdpName, Pattern pdpPattern) {
		this.pdpName = pdpName;
		this.pdpPattern = pdpPattern;
	}

	/**
	 * @return the pdpPattern
	 */
	public Pattern getPdpPattern() {
		return pdpPattern;
	}

	/**
	 * @param pdpPattern
	 *            the pdpPattern to set
	 */
	public void setPdpPattern(Pattern pdpPattern) {
		this.pdpPattern = pdpPattern;
	}

	/**
	 * @return the pdpName
	 */
	public String getPdpName() {
		return pdpName;
	}

	/**
	 * @param pdpName
	 *            the pdpName to set
	 */
	public void setPdpName(String pdpName) {
		this.pdpName = pdpName;
	}

	/**
	 * The value for the string.
	 * 
	 * @param string
	 *            the string.
	 * @return the data
	 */
	public static PDPData getValue(String string) {
		for (PDPData pdpData : PDPData.values()) {
			if (pdpData.getPdpName().equals(string)) {
				return pdpData;
			}
		}
		return null;
	}

}
