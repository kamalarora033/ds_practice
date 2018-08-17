package com.ericsson.fdp.business.enums;

/**
 * This enum defines the operating mode.
 * 
 * @author Ericsson
 * 
 */
public enum OperatingMode {

	ONLINE("Online"), OFFLINE("Offline");

	private String text;

	private OperatingMode(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

}
