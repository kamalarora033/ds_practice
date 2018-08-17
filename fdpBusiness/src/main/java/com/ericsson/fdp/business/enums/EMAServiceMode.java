package com.ericsson.fdp.business.enums;

/**
 * The Enum EMAServiceMode.
 */
public enum EMAServiceMode {

	/** The two g. */
	TWO_G,
	/** The three g. */
	THREE_G,
	
	SERVICE_ACTIVATION,
	
	BLACKBERRY_SERVICE_ACTIVATION
	;

	/**
	 * Gets the eMA service mode.
	 *
	 * @param mode the mode
	 * @return the eMA service mode
	 */
	public static EMAServiceMode getEMAServiceMode(final String mode) {
		EMAServiceMode emaServiceModeToReturn = null;
		for (EMAServiceMode emaServiceMode : EMAServiceMode.values()) {
			if (emaServiceMode.name().equals(mode)) {
				emaServiceModeToReturn = emaServiceMode;
			}
		}
		return emaServiceModeToReturn;
	}
}
