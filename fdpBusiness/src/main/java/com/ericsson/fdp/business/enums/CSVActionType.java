package com.ericsson.fdp.business.enums;

/**
 * The Enum CSVActionType.
 *
 * @author Ericsson
 */
public enum CSVActionType {

	/** The insert. */
	INSERT("INSERT"),

	/** The delete. */
	DELETE("DELETE");

	/** The action type. */
	private String actionType;

	/**
	 * Instantiates a new cSV action type.
	 *
	 * @param actionType the action type
	 */
	private CSVActionType(final String actionType) {
		this.actionType = actionType;
	}

	public static CSVActionType getCSVActionType(final String actionType) {
		CSVActionType actionTypeEnumToReturn = null;

		for (CSVActionType actionTypeEnum : CSVActionType.values()) {
			if (actionTypeEnum.getActionType().equals(actionType)) {
				actionTypeEnumToReturn = actionTypeEnum;
				break;
			}
		}
		return actionTypeEnumToReturn;
	}

	/**
	 * @return the actionType
	 */
	public String getActionType() {
		return actionType;
	}

	/**
	 * @param actionType the actionType to set
	 */
	public void setActionType(final String actionType) {
		this.actionType = actionType;
	}
}
