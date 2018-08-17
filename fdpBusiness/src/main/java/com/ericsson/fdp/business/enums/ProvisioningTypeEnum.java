package com.ericsson.fdp.business.enums;

public enum ProvisioningTypeEnum {

	PAM("P"), RS("R"), ADHOC("A");

	private String type;

	private ProvisioningTypeEnum(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
