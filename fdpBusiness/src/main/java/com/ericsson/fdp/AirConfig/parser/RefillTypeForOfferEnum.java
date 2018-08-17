package com.ericsson.fdp.AirConfig.parser;

public enum RefillTypeForOfferEnum {

	Account(0), Timer(2);

	private int value;

	RefillTypeForOfferEnum(int val) {
		this.value = val;
	}

	public int getValue() {
		return value;
	}

	public static String getNameByValue(String code) {
		for (RefillTypeForOfferEnum e : RefillTypeForOfferEnum.values()) {
			if (code.equals(Integer.toString(e.getValue())))
				return e.name();
		}
		return null;
	}
}
